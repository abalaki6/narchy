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

package spacegraph.phys.constraint;

import spacegraph.math.v3;

/**
 * 1D constraint along a normal axis between bodyA and bodyB. It can be combined
 * to solve contact and friction constraints.
 * 
 * @author jezek2
 */
public class SolverConstraint {

	public final v3 relpos1CrossNormal = new v3();
	public final v3 contactNormal = new v3();

	public final v3 relpos2CrossNormal = new v3();
	public final v3 angularComponentA = new v3();

	public final v3 angularComponentB = new v3();
	
	public float appliedPushImpulse;
	
	public float appliedImpulse;
	public int solverBodyIdA;
	public int solverBodyIdB;
	
	public float friction;
	public float restitution;
	public float jacDiagABInv;
	public float penetration;
	
	public SolverConstraintType constraintType;
	public int frictionIndex;
	public Object originalContactPoint;

	/**
     * Solver constraint type.
     *
     * @author jezek2
     */
    public enum SolverConstraintType {
        SOLVER_CONTACT_1D,
        SOLVER_FRICTION_1D
    }
}