package nars.derive;

import jcog.Util;
import nars.$;
import nars.NAR;
import nars.Param;
import nars.Task;
import nars.control.Cause;
import nars.control.CauseChannel;
import nars.control.Derivation;
import nars.op.DepIndepVarIntroduction;
import nars.task.DebugDerivedTask;
import nars.task.DerivedTask;
import nars.task.NALTask;
import nars.term.InvalidTermException;
import nars.term.Term;
import nars.term.Termed;
import nars.time.Tense;
import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.collections.api.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;
import java.util.function.BiFunction;

import static nars.Op.GOAL;
import static nars.Param.FILTER_SIMILAR_DERIVATIONS;
import static nars.time.Tense.ETERNAL;

/**
 * Final conclusion step of the derivation process that produces a derived task
 * <p>
 * Each rule corresponds to a unique instance of this
 * <p>
 * runtime instance. each leaf of each NAR's derivation tree should have
 * a unique instance, assigned the appropriate cause id by the NAR
 * at initialization.
 */
public class Conclusion extends AbstractPred<Derivation> {

    /**
     * destination of any derived tasks; also may be used to communicate backpressure
     * from the recipient.
     */
    public final CauseChannel<Task> channel;

    private final static Logger logger = LoggerFactory.getLogger(Conclusion.class);
    private final Term pattern;
    private final String rule;
    private final boolean varIntro, goalUrgent;
    private final int minNAL;

    public Conclusion(@NotNull Conclude id, CauseChannel<Task> input) {
        super($.func("derive", /*$.the(cid), */id.sub(0) /* prod args */));
        this.channel = input;
        this.pattern = id.pattern;
        this.varIntro = id.varIntro;
        this.goalUrgent = id.goalUrgent;
        this.rule = id.rule.toString(); //only store toString of the rule to avoid remaining attached to the RuleSet
        this.minNAL = id.rule.minNAL;
        //assert(this.minNAL!=0): "unknown min NAL level for rule: " + rule;
    }

    /**
     * @return true to allow the matcher to continue matching,
     * false to stop it
     */
    @Override
    public final boolean test(@NotNull Derivation p) {

        NAR nar = p.nar;

        nar.emotion.derivationTry.increment();
        p.use(Param.TTL_DERIVE_TRY);

        Term c1 = pattern.transform(p); //SUBSTITUTE and EVAL

        nar.emotion.derivationEval.increment();

        int volMax = nar.termVolumeMax.intValue();
        if (c1 == null || c1 == pattern || !c1.op().conceptualizable || c1.varPattern() > 0 || c1.volume() > volMax)
            return true;

        @NotNull final long[] occ;
        final float[] confGain = {1f}; //flat by default

        Term c2;
        if (p.temporal) {

            Term t1;
            try {
                occ = new long[]{ETERNAL, ETERNAL};
                t1 = solveTime(p, c1, occ, confGain);


//                if (t1!=null && occ[0] == 7) {
//                    //FOR A SPECIFIC TEST TEMPORAR
//                    System.err.println("wtf");
//                    solveTime(d, c1, occ, eviGain);
//                }
            } catch (InvalidTermException t) {
                if (Param.DEBUG) {
                    logger.error("temporalize error: {} {} {}", p, c1, t.getMessage());
                }
                return true;
            }

            //invalid or impossible temporalization; could not determine temporal attributes. seems this can happen normally
            if (t1 == null || t1.volume() > volMax || !t1.op().conceptualizable/*|| (Math.abs(occReturn[0]) > 2047483628)*/ /* long cast here due to integer wraparound */) {
//                            throw new InvalidTermException(c1.op(), c1.dt(), "temporalization failure"
//                                    //+ (Param.DEBUG ? rule : ""), c1.toArray()
//                            );

                //FOR DEBUGGING
//                if (t1==null)
//                    new Temporalize(d.random).solve(d, c1, new long[]{ETERNAL, ETERNAL});

                return true;
            }

            if (occ[1] == ETERNAL) occ[1] = occ[0]; //HACK probbly isnt needed

            if (goalUrgent && p.concPunc == GOAL) {
                long taskStart = p.task.start();

                if (p.temporal && taskStart == ETERNAL)
                    taskStart = p.time;

                //if (taskStart != ETERNAL) {
                if (occ[0] != ETERNAL && taskStart != ETERNAL && occ[0] < taskStart) {

                    long taskDur = occ[1] - occ[0];
                    occ[0] = taskStart;
                    occ[1] = occ[0] + taskDur;

                }
            }

            c2 = t1;

        } else {
            occ = Tense.ETERNAL_RANGE;
            c2 = c1;
        }

        c2 = c2.normalize();
        if (c2 == null)
            return true;

        // 3. VAR INTRO
        if (varIntro) {
            //var intro before temporalizing.  otherwise any calculated temporal data may not applied to the changed term (ex: occ shift)
            @Nullable Pair<Term, Map<Term, Term>> vc = DepIndepVarIntroduction.varIntroX(c2, p.random);
            if (vc == null) return true;

            Term v = vc.getOne();
            if (!(v.op().conceptualizable) || (v.equals(c2) /* keep only if it differs */))
                return true;

//            if (d.temporal) {
//                Map<Term, Term> m = vc.getTwo();
//                m.forEach(d.xy::tryPut); //store the mapping so that temporalization can resolve with it
//            }


            c2 = v;

        }

        //5. VALIDATE FOR TASK TERM

        byte punc = p.concPunc; assert (punc != 0) : "no punctuation assigned";

        Task t = Task.tryTask(c2, punc, p.concTruth, (C, tr) -> {

            long start = occ[0];
            long end = occ[1];
            assert (end >= start);

            short[] cause = ArrayUtils.addAll(p.parentCause, channel.id);

            long[] evi = p.single ? p.evidenceSingle() : p.evidenceDouble();

            long now = p.time;

            DerivedTask derived =
                    Param.DEBUG ?
                            new DebugDerivedTask(C, punc, tr, now, start, end, evi, cause, p.task, !p.single ? p.belief : null) :
                            new DerivedTask(C, punc, tr, now, start, end, evi, cause);


            return derived;
        });

        if (t == null) {
            return spam(p, Param.TTL_DERIVE_TASK_FAIL, c2, nar, 0);
        }

        if (same(t, p.task, p.truthResolution) || (p.belief != null && same(t, p.belief, p.truthResolution))) {
            //created a duplicate of the task
            return spam(p, Param.TTL_DERIVE_TASK_SAME, t, nar,0);
        }


        float priority = nar.derivePriority(t, p);
        assert (priority == priority);

        float tp = t.setPri(priority);

        if (Param.DEBUG)
            t.log(rule);

        if (p.derivations.merge(t, t, DUPLICATE_DERIVATION_MERGE) != t) {
            spam(p, Param.TTL_DERIVE_TASK_REPEAT, t, nar, 0);
        } else {
            p.use(Param.TTL_DERIVE_TASK_SUCCESS);
        }

        return true;
    }

    private boolean spam(@NotNull Derivation p, int cost, Termed t, NAR nar, float factor) {
        p.use(cost);
        if (factor > 0)
            channel.purpose[Cause.Purpose.Input.ordinal()].addAndGet(Param.inputCost(t, nar)*factor);
        return true; //just does
    }

    final static BiFunction<Task, Task, Task> DUPLICATE_DERIVATION_MERGE = (pp, tt) -> {
        pp.priMax(tt.pri());
        ((NALTask) pp).causeMerge(tt);
        return pp;
    };

    @Nullable
    private static Term solveTime(@NotNull Derivation d, Term c1, @NotNull long[] occ, float[] confGain) {
        DerivationTemporalize dt = d.temporalize;
        if (dt == null) {
            d.temporalize = dt = new DerivationTemporalize(d); //cache in derivation
        }
//        dt = new DerivationTemporalize(d);
        return dt.solve(d, c1, occ, confGain);
    }

    boolean same(Task derived, Task parent, float truthResolution) {
        if (parent.isDeleted())
            return false;

        if (derived.equals(parent)) return true;

        if (FILTER_SIMILAR_DERIVATIONS) {
            //test for same punc, term, start/end, freq, but different conf
            if (parent.term().equals(derived.term()) && parent.punc() == derived.punc() && parent.start() == derived.start() && parent.end() == derived.end()) {
                if (Arrays.equals(derived.stamp(), parent.stamp())) {
                    if (parent.isQuestOrQuestion() ||
                            (Util.equals(parent.freq(), derived.freq(), truthResolution) &&
                                    parent.evi() >= derived.evi())
                            ) {
                        if (Param.DEBUG_SIMILAR_DERIVATIONS)
                            logger.warn("similar derivation to parent:\n\t{} {}\n\t{}", derived, parent, rule);

                        //((NALTask)parent).merge(derived);
                        parent.priMax(derived.priElseZero());
                        ((NALTask) parent).causeMerge(derived); //merge cause
                        return true;
                    }
                }
            }
        }
        return false;
    }


}
