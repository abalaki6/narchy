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

package spacegraph.phys.solve;

import spacegraph.math.v3;
import spacegraph.phys.Dynamic;
import spacegraph.phys.math.Transform;
import spacegraph.phys.math.TransformUtil;

/**
 * SolverBody is an internal data structure for the constraint solver. Only necessary
 * data is packed to increase cache coherence/performance.
 * 
 * @author jezek2
 */
public class SolverBody {
	
	//protected final BulletStack stack = BulletStack.get();

	public final v3 angularVelocity = new v3();
	public float angularFactor;
	public float invMass;
	public float friction;
	public Dynamic body;
	public final v3 linearVelocity = new v3();
	public final v3 centerOfMassPosition = new v3();

	final Transform newTransform = new Transform();
	public final v3 pushVelocity = new v3();
	public final v3 turnVelocity = new v3();
	
	public void getVelocityInLocalPoint(v3 rel_pos, v3 velocity) {
		v3 tmp = new v3();
		tmp.cross(angularVelocity, rel_pos);
		velocity.add(linearVelocity, tmp);
	}

	/**
	 * Optimization for the iterative solver: avoid calculating constant terms involving inertia, normal, relative position.
	 */
	public void internalApplyImpulse(v3 linearComponent, v3 angularComponent, float impulseMagnitude) {
		if (invMass != 0f) {
			linearVelocity.scaleAdd(impulseMagnitude, linearComponent, linearVelocity);
			angularVelocity.scaleAdd(impulseMagnitude * angularFactor, angularComponent, angularVelocity);
		}
	}

	public void internalApplyPushImpulse(v3 linearComponent, v3 angularComponent, float impulseMagnitude) {
		if (invMass != 0f) {
			pushVelocity.scaleAdd(impulseMagnitude, linearComponent, pushVelocity);
			turnVelocity.scaleAdd(impulseMagnitude * angularFactor, angularComponent, turnVelocity);
		}
	}
	
	public void writebackVelocity() {
		if (invMass != 0f) {
			body.setLinearVelocity(linearVelocity);
			body.setAngularVelocity(angularVelocity);
			//m_originalBody->setCompanionId(-1);
		}
	}

	public void writebackVelocity(float timeStep) {
		if (invMass != 0f) {
			body.setLinearVelocity(linearVelocity);
			body.setAngularVelocity(angularVelocity);

			// correct the position/orientation based on push/turn recovery
			newTransform.setIdentity();
			Transform curTrans = body.transform; //getWorldTransform(new Transform());
			TransformUtil.integrateTransform(curTrans, pushVelocity, turnVelocity, timeStep, newTransform);
			body.transform(newTransform);

			//m_originalBody->setCompanionId(-1);
		}
	}
	
	public void readVelocity() {
		if (invMass != 0f) {
			body.getLinearVelocity(linearVelocity);
			body.getAngularVelocity(angularVelocity);
		}
	}
	
}
