/*
 * Copyright 2015 S. Webber
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.oakgp.examples.ant;

import org.oakgp.Arguments;
import org.oakgp.Assignments;
import org.oakgp.function.ImpureFunction;
import org.oakgp.function.Signature;
import org.oakgp.node.FunctionNode;
import org.oakgp.node.Node;
import org.oakgp.util.Void;

import static org.oakgp.examples.ant.AntMovement.isLeftAndRight;
import static org.oakgp.util.Void.*;

/**
 * Executes two nodes in sequence.
 */
class BiSequence implements ImpureFunction {
    static final BiSequence BISEQUENCE = new BiSequence();

    private BiSequence() {
    }

    @Override
    public Signature sig() {
        return new Signature(VOID_TYPE, VOID_TYPE, VOID_TYPE);
    }

    @Override
    public Void evaluate(Arguments arguments, Assignments assignments) {
        arguments.firstArg().eval(assignments);
        arguments.secondArg().eval(assignments);
        return Void.VOID;
    }

    @Override
    public Node simplify(Arguments arguments) {
        Node firstArg = arguments.firstArg();
        Node secondArg = arguments.secondArg();
        if (isVoid(firstArg)) {
            return secondArg;
        } else if (isVoid(secondArg)) {
            return firstArg;
        } else if (isLeftAndRight(firstArg, secondArg)) {
            return VOID_CONSTANT;
        } else if (isBiSequence(firstArg)) {
            Arguments firstArgArgs = ((FunctionNode) firstArg).args();
            return createTriSequence(firstArgArgs.firstArg(), firstArgArgs.secondArg(), secondArg);
        } else if (isBiSequence(secondArg)) {
            Arguments secondArgArgs = ((FunctionNode) secondArg).args();
            return createTriSequence(firstArg, secondArgArgs.firstArg(), secondArgArgs.secondArg());
        } else {
            return null;
        }
    }

    private boolean isBiSequence(Node firstArg) {
        FunctionNode fn = (FunctionNode) firstArg;
        return fn.func() == BISEQUENCE;
    }

    private Node createTriSequence(Node arg1, Node arg2, Node arg3) {
        return new FunctionNode(TriSequence.TRISEQUENCE, arg1, arg2, arg3);
    }
}
