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

package spacegraph.phys.shape;

import spacegraph.math.Matrix3f;
import spacegraph.math.v3;
import spacegraph.phys.collision.broad.BroadphaseNativeType;
import spacegraph.phys.math.MatrixUtil;
import spacegraph.phys.math.Transform;
import spacegraph.phys.math.VectorUtil;
import spacegraph.phys.util.OArrayList;

import java.util.Collection;

// JAVA NOTE: CompoundShape from 2.71

/**
 * CompoundShape allows to store multiple other {@link CollisionShape}s. This allows
 * for moving concave collision objects. This is more general than the {@link BvhTriangleMeshShape}.
 * 
 * @author jezek2
 */
public class CompoundShape extends CollisionShape {

	private final OArrayList<CompoundShapeChild> children = new OArrayList<>();
	private final v3 localAabbMin = new v3(1e30f, 1e30f, 1e30f);
	private final v3 localAabbMax = new v3(-1e30f, -1e30f, -1e30f);

	private final OptimizedBvh aabbTree = new OptimizedBvh();

	private float collisionMargin;
	protected final v3 localScaling = new v3(1f, 1f, 1f);

	public void addChildShape(Transform localTransform, CollisionShape shape) {
		//m_childTransforms.push_back(localTransform);
		//m_childShapes.push_back(shape);
		CompoundShapeChild child = new CompoundShapeChild(shape);
		child.transform.set(localTransform);
		//child.childMargin = shape.getMargin();

		children.add(child);

		// extend the local aabbMin/aabbMax
		v3 _localAabbMin = new v3(), _localAabbMax = new v3();
		shape.getAabb(localTransform, _localAabbMin, _localAabbMax);

		// JAVA NOTE: rewritten
//		for (int i=0;i<3;i++)
//		{
//			if (this.localAabbMin[i] > _localAabbMin[i])
//			{
//				this.localAabbMin[i] = _localAabbMin[i];
//			}
//			if (this.localAabbMax[i] < _localAabbMax[i])
//			{
//				this.localAabbMax[i] = _localAabbMax[i];
//			}
//		}
		VectorUtil.setMin(this.localAabbMin, _localAabbMin);
		VectorUtil.setMax(this.localAabbMax, _localAabbMax);
	}

	/**
	 * Remove all children shapes that contain the specified shape.
	 */
	public void removeChildShape(CollisionShape shape) {
		boolean done_removing;

		// Find the children containing the shape specified, and remove those children.
		do {
			done_removing = true;

			for (int i = 0; i < children.size(); i++) {
				//return array[index];
				if (children.get(i).childShape == shape) {
					children.removeFast(i);
					done_removing = false;  // Do another iteration pass after removing from the vector
					break;
				}
			}
		}
		while (!done_removing);

		recalculateLocalAabb();
	}
	
	public int getNumChildShapes() {
		return children.size();
	}

	public CollisionShape getChildShape(int index) {
		//return array[index];
		return children.get(index).childShape;
	}

	public Transform getChildTransform(int index, Transform out) {
		//return array[index];
		Transform t = children.get(index).transform;
		out.set(t);
		return out;
	}

	public Collection<CompoundShapeChild> getChildList() {
		return children;
	}

	/**
	 * getAabb's default implementation is brute force, expected derived classes to implement a fast dedicated version.
	 */
	@Override
	public void getAabb(Transform trans, v3 aabbMin, v3 aabbMax) {
		v3 localHalfExtents = new v3();
		localHalfExtents.sub(localAabbMax, localAabbMin);
		localHalfExtents.scale(0.5f);
		localHalfExtents.x += collisionMargin;
		localHalfExtents.y += collisionMargin;
		localHalfExtents.z += collisionMargin;

		v3 localCenter = new v3();
		localCenter.add(localAabbMax, localAabbMin);
		localCenter.scale(0.5f);

		Matrix3f abs_b = new Matrix3f(trans.basis);
		MatrixUtil.absolute(abs_b);

		v3 center = new v3(localCenter);
		trans.transform(center);

		v3 tmp = new v3();

		v3 extent = new v3();
		abs_b.getRow(0, tmp);
		extent.x = tmp.dot(localHalfExtents);
		abs_b.getRow(1, tmp);
		extent.y = tmp.dot(localHalfExtents);
		abs_b.getRow(2, tmp);
		extent.z = tmp.dot(localHalfExtents);

		aabbMin.sub(center, extent);
		aabbMax.add(center, extent);
	}

	/**
	 * Re-calculate the local Aabb. Is called at the end of removeChildShapes.
	 * Use this yourself if you modify the children or their transforms.
	 */
	public void recalculateLocalAabb() {
		// Recalculate the local aabb
		// Brute force, it iterates over all the shapes left.
		localAabbMin.set(1e30f, 1e30f, 1e30f);
		localAabbMax.set(-1e30f, -1e30f, -1e30f);

		v3 tmpLocalAabbMin = new v3();
		v3 tmpLocalAabbMax = new v3();

		// extend the local aabbMin/aabbMax
		for (int j = 0; j < children.size(); j++) {
			//return array[index];
			//return array[index];
			children.get(j).childShape.getAabb(children.get(j).transform, tmpLocalAabbMin, tmpLocalAabbMax);
			
			for (int i = 0; i < 3; i++) {
				if (VectorUtil.coord(localAabbMin, i) > VectorUtil.coord(tmpLocalAabbMin, i)) {
					VectorUtil.setCoord(localAabbMin, i, VectorUtil.coord(tmpLocalAabbMin, i));
				}
				if (VectorUtil.coord(localAabbMax, i) < VectorUtil.coord(tmpLocalAabbMax, i)) {
					VectorUtil.setCoord(localAabbMax, i, VectorUtil.coord(tmpLocalAabbMax, i));
				}
			}
		}
	}
	
	@Override
	public void setLocalScaling(v3 scaling) {
		localScaling.set(scaling);
	}

	@Override
	public v3 getLocalScaling(v3 out) {
		out.set(localScaling);
		return out;
	}

	@Override
	public void calculateLocalInertia(float mass, v3 inertia) {
		// approximation: take the inertia from the aabb for now
		Transform ident = new Transform();
		ident.setIdentity();
		v3 aabbMin = new v3(), aabbMax = new v3();
		getAabb(ident, aabbMin, aabbMax);

		v3 halfExtents = new v3();
		halfExtents.sub(aabbMax, aabbMin);
		halfExtents.scale(0.5f);

		float lx = 2f * halfExtents.x;
		float ly = 2f * halfExtents.y;
		float lz = 2f * halfExtents.z;

		inertia.x = (mass / 12f) * (ly * ly + lz * lz);
		inertia.y = (mass / 12f) * (lx * lx + lz * lz);
		inertia.z = (mass / 12f) * (lx * lx + ly * ly);
	}
	
	@Override
	public BroadphaseNativeType getShapeType() {
		return BroadphaseNativeType.COMPOUND_SHAPE_PROXYTYPE;
	}

	@Override
	public GImpactShape setMargin(float margin) {
		collisionMargin = margin;
		return null;
	}

	@Override
	public float getMargin() {
		return collisionMargin;
	}

	@Override
	public String getName() {
		return "Compound";
	}

	// this is optional, but should make collision queries faster, by culling non-overlapping nodes
	// void	createAabbTreeFromChildren();
	
	public OptimizedBvh getAabbTree() {
		return aabbTree;
	}

	/**
	 * Computes the exact moment of inertia and the transform from the coordinate
	 * system defined by the principal axes of the moment of inertia and the center
	 * of mass to the current coordinate system. "masses" points to an array
	 * of masses of the children. The resulting transform "principal" has to be
	 * applied inversely to all children transforms in order for the local coordinate
	 * system of the compound shape to be centered at the center of mass and to coincide
	 * with the principal axes. This also necessitates a correction of the world transform
	 * of the collision object by the principal transform.
	 */
	public void calculatePrincipalAxisTransform(float[] masses, Transform principal, v3 inertia) {
		int n = children.size();

		float totalMass = 0;
		v3 center = new v3();
		center.set(0, 0, 0);
		for (int k = 0; k < n; k++) {
			//return array[index];
			center.scaleAdd(masses[k], children.get(k).transform, center);
			totalMass += masses[k];
		}
		center.scale(1f / totalMass);
		principal.set(center);

		Matrix3f tensor = new Matrix3f();
		tensor.setZero();

		for (int k = 0; k < n; k++) {
			v3 i = new v3();
			//return array[index];
			children.get(k).childShape.calculateLocalInertia(masses[k], i);

			//return array[index];
			Transform t = children.get(k).transform;
			v3 o = new v3();
			o.sub(t, center);

			// compute inertia tensor in coordinate system of compound shape
			Matrix3f j = new Matrix3f();
			j.transpose(t.basis);

			j.m00 *= i.x;
			j.m01 *= i.x;
			j.m02 *= i.x;
			j.m10 *= i.y;
			j.m11 *= i.y;
			j.m12 *= i.y;
			j.m20 *= i.z;
			j.m21 *= i.z;
			j.m22 *= i.z;

			j.mul(t.basis, j);

			// add inertia tensor
			tensor.add(j);

			// compute inertia tensor of pointmass at o
			float o2 = o.lengthSquared();
			j.setRow(0, o2, 0, 0);
			j.setRow(1, 0, o2, 0);
			j.setRow(2, 0, 0, o2);
			j.m00 += o.x * -o.x;
			j.m01 += o.y * -o.x;
			j.m02 += o.z * -o.x;
			j.m10 += o.x * -o.y;
			j.m11 += o.y * -o.y;
			j.m12 += o.z * -o.y;
			j.m20 += o.x * -o.z;
			j.m21 += o.y * -o.z;
			j.m22 += o.z * -o.z;

			// add inertia tensor of pointmass
			tensor.m00 += masses[k] * j.m00;
			tensor.m01 += masses[k] * j.m01;
			tensor.m02 += masses[k] * j.m02;
			tensor.m10 += masses[k] * j.m10;
			tensor.m11 += masses[k] * j.m11;
			tensor.m12 += masses[k] * j.m12;
			tensor.m20 += masses[k] * j.m20;
			tensor.m21 += masses[k] * j.m21;
			tensor.m22 += masses[k] * j.m22;
		}

		MatrixUtil.diagonalize(tensor, principal.basis, 0.00001f, 20);

		inertia.set(tensor.m00, tensor.m11, tensor.m22);
	}

}
