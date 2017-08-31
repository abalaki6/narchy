package nars.derive.op;

import nars.$;
import nars.Op;
import nars.control.Derivation;
import nars.derive.AbstractPred;
import nars.derive.PrediTerm;

import static nars.Op.CONJ;
import static nars.time.Tense.DTERNAL;
import static nars.time.Tense.XTERNAL;

/**
 * Created by me on 5/19/17.
 */
public final class TaskBeliefOp extends AbstractPred<Derivation> {
    private final byte op;
    private final boolean task;
    private final boolean belief;

    public TaskBeliefOp(Op op, boolean testTask, boolean testBelief) {
        super($.func("op", $.quote(op.str), $.the(testTask ? 1 : 0), $.the(testBelief ? 1 : 0)));
        this.op = op.id;
        this.task = testTask;
        this.belief = testBelief;
    }

    @Override
    public boolean test(Derivation derivation) {
        return (!task || derivation.termSub0op == op)
                &&
                (!belief || derivation.termSub1op == op);
    }

    public static class TaskBeliefConjSeq extends AbstractPred<Derivation> {

        private final boolean task;
        private final boolean belief;

        public TaskBeliefConjSeq(boolean testTask, boolean testBelief) {
            super($.func("conjSeq", $.the(testTask ? 1 : 0), $.the(testBelief ? 1 : 0)));
            this.task = testTask;
            this.belief = testBelief;
        }

        @Override
        public boolean test(Derivation derivation) {
            return (!task || (derivation.termSub0op == CONJ.id && isSequence(derivation.taskTerm.dt()))
                    &&
                    (!belief || derivation.termSub1op == CONJ.id && isSequence(derivation.beliefTerm.dt())));
        }

        static boolean isSequence(int dt) {
            return dt!=0 && dt!=DTERNAL && dt!=XTERNAL;
        }


    }
}
