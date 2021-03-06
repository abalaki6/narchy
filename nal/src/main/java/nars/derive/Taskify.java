package nars.derive;

import jcog.Util;
import nars.$;
import nars.Param;
import nars.Task;
import nars.control.Derivation;
import nars.task.DebugDerivedTask;
import nars.task.DerivedTask;
import nars.task.NALTask;
import nars.task.TimeFusion;
import nars.term.Term;
import nars.term.pred.AbstractPred;
import nars.truth.Truth;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiFunction;

import static nars.Param.FILTER_SIMILAR_DERIVATIONS;

public class Taskify extends AbstractPred<Derivation> {

    final static BiFunction<DerivedTask, DerivedTask, DerivedTask> DUPLICATE_DERIVATION_MERGE = (pp, tt) -> {
        pp.priMax(tt.pri());
        pp.causeMerge(tt);
        return pp;
    };
    private final static Logger logger = LoggerFactory.getLogger(Taskify.class);

    /**
     * destination of any derived tasks; also may be used to communicate backpressure
     * from the recipient.
     */
    public final Conclude.RuleCause channel;

    protected Taskify(Conclude.RuleCause channel) {
        super($.func("taskify", $.the(channel.id)));
        this.channel = channel;
    }

    @Override
    public boolean test(Derivation d) {

        Term x0 = d.derivedTerm.get();
        Term x = d.anon.get(x0);
        if (x == null || !x.op().conceptualizable)
            return false; //when the values were finally dereferenced, the result produced an invalid compound

        long[] occ = d.concOcc;
        byte punc = d.concPunc;
        assert (punc != 0) : "no punctuation assigned";

        Truth tru = d.concTruth;
        if (tru!=null) {
            tru = tru.ditherDiscrete(d.freqRes, d.confRes, d.confMin,
                    //TimeFusion.eviEternalize(tru.evi(), d.concEviFactor)
                    tru.evi() * d.concEviFactor
            );
            if (tru == null)
                return false;
        }

        DerivedTask t = (DerivedTask) Task.tryTask(x, punc, tru, (C, tr) -> {

            long start = occ[0];
            long end = occ[1];
            assert (end >= start): "task has reversed occurrence: " + start + ".." + end;

            long[] evi = d.single ? d.evidenceSingle() : d.evidenceDouble();
            long now = d.time;
            return Param.DEBUG ?
                            new DebugDerivedTask(C, punc, tr, now, start, end, evi, d._task, !d.single ? d._belief : null) :
                            new DerivedTask(C, punc, tr, now, start, end, evi);
        });

        if (t == null) {
            return spam(d, Param.TTL_DERIVE_TASK_FAIL);
        }

        if (d.single)
            t.setCyclic(true);

        if (same(t, d._task, d.freqRes) || (d._belief != null && same(t, d._belief, d.freqRes))) {
            //created a duplicate of the task
            return spam(d, Param.TTL_DERIVE_TASK_SAME);
        }

        float priority = Param.derivationPriority(t, d)
                //* channel.amp()
        ;

        assert (priority == priority);

        t.priSet(priority);

        if (Param.DEBUG)
            t.log(channel.ruleString);

        t.cause = ArrayUtils.addAll(d.parentCause, channel.id);

        if (d.derivations.merge(t, t, DUPLICATE_DERIVATION_MERGE) != t) {
            spam(d, Param.TTL_DERIVE_TASK_REPEAT);
        } else {
            d.use(Param.TTL_DERIVE_TASK_SUCCESS);
        }

        return true;
    }


    private static boolean spam(Derivation p, int cost) {
        p.use(cost);
        return true; //just does
    }

    protected boolean same(Task derived, Task parent, float truthResolution) {
        if (parent.isDeleted())
            return false;

        if (derived.equals(parent)) return true;

        if (FILTER_SIMILAR_DERIVATIONS) {
            //test for same punc, term, start/end, freq, but different conf
            if (parent.term().equals(derived.term()) && parent.punc() == derived.punc() && parent.start() == derived.start() && parent.end() == derived.end()) {
                /*if (Arrays.equals(derived.stamp(), parent.stamp()))*/
                if (parent.isQuestOrQuestion() ||
                        (Util.equals(parent.freq(), derived.freq(), truthResolution) &&
                                parent.evi() >= derived.evi())
                        ) {
                    if (Param.DEBUG_SIMILAR_DERIVATIONS)
                        logger.warn("similar derivation to parent:\n\t{} {}\n\t{}", derived, parent, channel.ruleString);


                    if (parent.isCyclic() && !derived.isCyclic())
                        parent.setCyclic(false);

                    if (parent instanceof DerivedTask) {
                        parent.priMax(derived.priElseZero());
                        ((NALTask) parent).causeMerge(derived); //merge cause
                    }
                    return true;
                }
            }
        }
        return false;
    }

}
