package nars.op.stm;

import nars.$;
import nars.NAR;
import nars.Task;
import nars.control.CauseChannel;
import nars.task.ITask;
import nars.task.NALTask;
import nars.term.Term;
import nars.term.atom.Bool;
import nars.truth.Truth;
import nars.truth.TruthFunctions;
import org.eclipse.collections.api.block.function.primitive.FloatFunction;
import org.jetbrains.annotations.NotNull;

import static nars.Op.BELIEF;

public class RelationClustering extends ChainClustering {

    private final CauseChannel<ITask> in;

    public RelationClustering(@NotNull NAR nar, FloatFunction<Task> accept, int centroids, int capacity) {
        super(nar, accept, centroids, capacity);
        in = nar.newCauseChannel(this);
    }

    @Override
    protected void link(Task tx, Task ty) {
        assert (tx.isBelief() && ty.isBelief()); //TODO abstract


        //TODO Allen interval
        String relation;
        if (tx.isDuring(ty.start(), ty.end()) && ty.isDuring(tx.start(), tx.end())) {
            relation = "simul";
        } else if (ty.isAfter(tx.end(), dur / 2)) {
            relation = "seq";
        } else if (tx.isAfter(ty.end(), dur / 2)) {
            Task z = tx;
            tx = ty;
            ty = z;
            relation = "seq";
        } else {
            relation = null;
        }

        if (relation != null) {
            Term x = tx.term();
            Truth truX = tx.truth();
            if (truX.isNegative()) {
                x = x.neg();
                truX = truX.neg();
            }
            Term y = ty.term();
            Truth truY = ty.truth();
            if (truY.isNegative()) {
                y = y.neg();
                truY = truY.neg();
            }

            if (x.volume() + y.volume() < nar.termVolumeMax.intValue() - 2) {
                Truth tru = TruthFunctions.intersection(truX, truY, nar.confMin.floatValue());
                if (tru == null)
                    return;

                //TODO enum
                Term t;
                switch (relation) {
                    case "simul":
                        t = $.inh($.sete(x, y), $.the("simul"));
                        break;
                    case "seq":
                        t = $.func(relation, x, y);
                        break;
                    default:
                        throw new UnsupportedOperationException();
                }

                if (t instanceof Bool)
                    return;

                t = t.normalize();

                long now = nar.time();
                NALTask tt = new NALTask(t, BELIEF, tru, now, Math.min(tx.start(), ty.start()),
                        Math.max(tx.end(), ty.end()), nar.time.nextInputStamp());
                tt.pri(tx.priElseZero() * ty.priElseZero());
                in.input(tt);
            }
        }
    }

}
