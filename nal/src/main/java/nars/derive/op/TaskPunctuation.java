package nars.derive.op;

import nars.control.ProtoDerivation;
import nars.term.pred.AbstractPred;
import nars.term.pred.PrediTerm;

import static nars.Op.*;

/**
 * Created by me on 8/27/15.
 */
final public class TaskPunctuation extends AbstractPred<ProtoDerivation> {

    public final byte punc;

    TaskPunctuation(byte p) {
        this(p, "task:\"" + ((char) p) + '\"');
    }

    TaskPunctuation(byte p, String id) {
        super(id);
        this.punc = p;
    }


    @Override
    public final boolean test(ProtoDerivation m) {
        return m.taskPunc == punc;
    }

    @Override
    public float cost() {
        return 0.1f;
    }

    public static final PrediTerm<ProtoDerivation> Belief = new TaskPunctuation(BELIEF);
    public static final PrediTerm<ProtoDerivation> Goal = new TaskPunctuation(GOAL);

    public static final PrediTerm<ProtoDerivation> BeliefOrGoal = new AbstractPred<ProtoDerivation>("task:\".!\"") {
        @Override
        public boolean test(ProtoDerivation o) {
            byte c = o.taskPunc;
            return c == BELIEF || c == GOAL;
        }

        @Override
        public float cost() {
            return 0.1f;
        }
    };


    public static final PrediTerm<ProtoDerivation> Question = new TaskPunctuation(QUESTION);

    public static final PrediTerm<ProtoDerivation> Quest = new TaskPunctuation(QUEST);


}
