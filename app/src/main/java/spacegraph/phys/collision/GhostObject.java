/*
 * Java port of Bullet (c) 2008 Martin Dvorak <jezek2@advel.cz>
 *
 * Bullet Continuous Collision Detection and Physics Library
 * Copyright (c) 2003-2008 Erwin Coumans  http://www.bulletphysics.com/
 *
 * This software is provided 'as-is', without any express or implied warranty.
 * In no event will the authors be held liable for any damages arising from
 * the use of this software.
 *
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 *
 * 1. The origin of this software must not be misrepresented; you must not
 *    claim that you wrote the original software. If you use this software
 *    in a product, an acknowledgment in the product documentation would be
 *    appreciated but is not required.
 * 2. Altered source versions must be plainly marked as such, and must not be
 *    misrepresented as being the original software.
 * 3. This notice may not be removed or altered from any source distribution.
 */

package spacegraph.phys.collision;

import spacegraph.math.Quat4f;
import spacegraph.math.v3;
import spacegraph.phys.Collidable;
import spacegraph.phys.Collisions;
import spacegraph.phys.collision.broad.Broadphasing;
import spacegraph.phys.collision.broad.Intersecter;
import spacegraph.phys.collision.narrow.VoronoiSimplexSolver;
import spacegraph.phys.math.AabbUtil2;
import spacegraph.phys.math.Transform;
import spacegraph.phys.math.TransformUtil;
import spacegraph.phys.shape.ConvexShape;
import spacegraph.phys.util.OArrayList;

/**
 * GhostObject can keep track of all objects that are overlapping. By default, this
 * overlap is based on the AABB. This is useful for creating a character controller,
 * collision sensors/triggers, explosions etc.
 *
 * @author tomrbryn
 */
public class GhostObject extends Collidable {

	protected final OArrayList<Collidable> overlappingObjects = new OArrayList<>();

	public GhostObject() {
		super(CollidableType.GHOST_OBJECT, new Transform());
	}

	/**
	 * This method is mainly for expert/internal use only.
	 */
	public void addOverlappingObjectInternal(Broadphasing otherProxy, Broadphasing thisProxy) {
		Collidable otherObject = otherProxy.data;
		assert(otherObject != null);

		// if this linearSearch becomes too slow (too many overlapping objects) we should add a more appropriate data structure
		int index = overlappingObjects.indexOf(otherObject);
		if (index == -1) {
			// not found
			overlappingObjects.add(otherObject);
		}
	}

	/**
	 * This method is mainly for expert/internal use only.
	 */
	public void removeOverlappingObjectInternal(Broadphasing otherProxy, Intersecter intersecter, Broadphasing thisProxy) {
		Collidable otherObject = otherProxy.data;
		assert(otherObject != null);

		OArrayList<Collidable> o = this.overlappingObjects;
		int index = o.indexOf(otherObject);
		if (index != -1) {
            //return array[index];
			int num = o.size();
			o.setQuick(index, o.get(num - 1));
			o.removeQuick(num -1);
		}
	}

	public void convexSweepTest(ConvexShape castShape, Transform convexFromWorld, Transform convexToWorld, Collisions.ConvexResultCallback resultCallback, float allowedCcdPenetration) {
		Transform convexFromTrans = new Transform();
		Transform convexToTrans = new Transform();

		convexFromTrans.set(convexFromWorld);
		convexToTrans.set(convexToWorld);

		v3 castShapeAabbMin = new v3();
		v3 castShapeAabbMax = new v3();

		// compute AABB that encompasses angular movement
        v3 linVel = new v3();
        v3 angVel = new v3();
        TransformUtil.calculateVelocity(convexFromTrans, convexToTrans, 1f, linVel, angVel);
        Transform R = new Transform();
        R.setIdentity();
        R.setRotation(convexFromTrans.getRotation(new Quat4f()));
        castShape.calculateTemporalAabb(R, linVel, angVel, 1f, castShapeAabbMin, castShapeAabbMax);

        Transform tmpTrans = new Transform();

		// go over all objects, and if the ray intersects their aabb + cast shape aabb,
		// do a ray-shape query using convexCaster (CCD)
		for (int i=0; i<overlappingObjects.size(); i++) {
            //return array[index];
            Collidable collidable = overlappingObjects.get(i);

			// only perform raycast if filterMask matches
			if (resultCallback.needsCollision(collidable.broadphase())) {
				//RigidcollisionObject* collisionObject = ctrl->GetRigidcollisionObject();
				v3 collisionObjectAabbMin = new v3();
				v3 collisionObjectAabbMax = new v3();
				collidable.shape().getAabb(collidable.getWorldTransform(tmpTrans), collisionObjectAabbMin, collisionObjectAabbMax);
				AabbUtil2.aabbExpand(collisionObjectAabbMin, collisionObjectAabbMax, castShapeAabbMin, castShapeAabbMax);
				float[] hitLambda = {1f}; // could use resultCallback.closestHitFraction, but needs testing
				v3 hitNormal = new v3();
				if (AabbUtil2.rayAabb(convexFromWorld, convexToWorld, collisionObjectAabbMin, collisionObjectAabbMax, hitLambda, hitNormal)) {
					Collisions.objectQuerySingle(castShape, convexFromTrans, convexToTrans,
                            collidable,
					                                 collidable.shape(),
					                                 collidable.getWorldTransform(tmpTrans),
					                                 resultCallback,
					                                 allowedCcdPenetration);
				}
			}
		}
	}

	public void rayTest(v3 rayFromWorld, v3 rayToWorld, Collisions.RayResultCallback resultCallback) {
		Transform rayFromTrans = new Transform();
		rayFromTrans.setIdentity();
		rayFromTrans.set(rayFromWorld);
		Transform rayToTrans = new Transform();
		rayToTrans.setIdentity();
		rayToTrans.set(rayToWorld);

		Transform tmpTrans = new Transform();

		VoronoiSimplexSolver solver = new VoronoiSimplexSolver();

		for (int i=0; i<overlappingObjects.size(); i++) {
            //return array[index];
            Collidable collidable = overlappingObjects.get(i);

			// only perform raycast if filterMask matches
			if (resultCallback.needsCollision(collidable.broadphase())) {
				Collisions.rayTestSingle(rayFromTrans, rayToTrans,
                        collidable,
				                             collidable.shape(),
				                             collidable.getWorldTransform(tmpTrans),
											 solver,
				                             resultCallback);
			}
		}
	}

	public int getNumOverlappingObjects() {
		return overlappingObjects.size();
	}

	public Collidable getOverlappingObject(int index) {
        return overlappingObjects.get(index);
        //return array[index];
    }

	public OArrayList<Collidable> getOverlappingPairs() {
		return overlappingObjects;
	}

	//
	// internal cast
	//

	public static GhostObject upcast(Collidable colObj) {
		if (colObj.getInternalType() == CollidableType.GHOST_OBJECT) {
			return (GhostObject)colObj;
		}
		
		return null;
	}
	
}
