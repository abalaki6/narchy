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

import spacegraph.math.v3;
import spacegraph.phys.BulletGlobals;
import spacegraph.phys.Collidable;
import spacegraph.phys.collision.broad.CollisionAlgorithm;
import spacegraph.phys.collision.broad.CollisionAlgorithmConstructionInfo;
import spacegraph.phys.collision.broad.DispatcherInfo;
import spacegraph.phys.collision.narrow.PersistentManifold;
import spacegraph.phys.shape.SphereShape;
import spacegraph.phys.util.OArrayList;

/**
 * Provides collision detection between two spheres.
 * 
 * @author jezek2
 */
public class SphereSphereCollisionAlgorithm extends CollisionAlgorithm {
	
	private boolean ownManifold;
	private PersistentManifold manifoldPtr;
	
	public void init(PersistentManifold mf, CollisionAlgorithmConstructionInfo ci, Collidable col0, Collidable col1) {
		super.init(ci);
		manifoldPtr = mf;

		if (manifoldPtr == null) {
			manifoldPtr = intersecter.getNewManifold(col0, col1);
			ownManifold = true;
		}
	}



	@Override
	public void destroy() {
		if (ownManifold && manifoldPtr != null) {
			intersecter.releaseManifold(manifoldPtr);
			manifoldPtr = null;
		}
	}

	@Override
	public void processCollision(Collidable col0, Collidable col1, DispatcherInfo dispatchInfo, ManifoldResult resultOut) {
		if (manifoldPtr == null) {
			return;
		}

		resultOut.setPersistentManifold(manifoldPtr);


		v3 diff = new v3();
		diff.sub(col0.transform, col1.transform);

		float lenSq = diff.lengthSquared();

		SphereShape sphere0 = (SphereShape) col0.shape();
		SphereShape sphere1 = (SphereShape) col1.shape();
		float radius0 = sphere0.getRadius();
		float radius1 = sphere1.getRadius();

		//#ifdef CLEAR_MANIFOLD
		//manifoldPtr.clearManifold(); // don't do this, it disables warmstarting
		//#endif

		// if distance positive, don't generate a new contact
		float r01 = radius0 + radius1;
		if (lenSq > r01*r01) {
			//#ifndef CLEAR_MANIFOLD
			resultOut.refreshContactPoints();
			//#endif //CLEAR_MANIFOLD
			return;
		}


		v3 normalOnSurfaceB = new v3(1, 0, 0);

		float len = (float) Math.sqrt(lenSq);
		// distance (negative means penetration)
		float dist = len - r01;

		if (len > BulletGlobals.FLT_EPSILON) {
			normalOnSurfaceB.scale(1f / len, diff);
		}

		v3 tmp = new v3();

		// point on A (worldspace)
		v3 pos0 = new v3();
		tmp.scale(radius0, normalOnSurfaceB);
		pos0.sub(col0.transform, tmp);

		// point on B (worldspace)
		v3 pos1 = new v3();
		tmp.scale(radius1, normalOnSurfaceB);
		pos1.add(col1.transform, tmp);

		// report a contact. internally this will be kept persistent, and contact reduction is done
		resultOut.addContactPoint(normalOnSurfaceB, pos1, dist, manifoldPtr.getContactBreakingThreshold());

		//#ifndef CLEAR_MANIFOLD
		resultOut.refreshContactPoints();
		//#endif //CLEAR_MANIFOLD
	}

	@Override
	public float calculateTimeOfImpact(Collidable body0, Collidable body1, DispatcherInfo dispatchInfo, ManifoldResult resultOut) {
		return 1f;
	}

	@Override
	public void getAllContactManifolds(OArrayList<PersistentManifold> manifoldArray) {
		if (manifoldPtr != null && ownManifold) {
			manifoldArray.add(manifoldPtr);
		}
	}

	////////////////////////////////////////////////////////////////////////////

	public static class CreateFunc extends CollisionAlgorithmCreateFunc {
		@Override
		public CollisionAlgorithm createCollisionAlgorithm(CollisionAlgorithmConstructionInfo ci, Collidable body0, Collidable body1) {
			SphereSphereCollisionAlgorithm algo = new SphereSphereCollisionAlgorithm();
			algo.init(null, ci, body0, body1);
			return algo;
		}

	}

}
