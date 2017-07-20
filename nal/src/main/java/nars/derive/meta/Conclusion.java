package nars.derive.meta;

import jcog.pri.Priority;
import nars.NAR;
import nars.Op;
import nars.Param;
import nars.Task;
import nars.control.CauseChannel;
import nars.control.premise.Derivation;
import nars.derive.rule.PremiseRule;
import nars.op.DepIndepVarIntroduction;
import nars.task.DebugDerivedTask;
import nars.task.DerivedTask;
import nars.term.Compound;
import nars.term.Term;
import nars.time.Tense;
import nars.time.TimeFunctions;
import nars.truth.Truth;
import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.collections.api.tuple.primitive.ObjectBooleanPair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static nars.Op.NEG;
import static nars.term.Terms.compoundOrNull;
import static nars.term.Terms.normalizedOrNull;
import static nars.time.Tense.ETERNAL;

/**
 * Final conclusion step of the derivation process that produces a derived task
 * <p>
 * Each rule corresponds to a unique instance of this
 *
 * runtime instance. each leaf of each NAR's derivation tree should have
 * a unique instance, assigned the appropriate cause id by the NAR
 * at initialization.
 */
public class Conclusion extends AbstractPred<Derivation> {

    /** destination of any derived tasks; also may be used to communicate backpressure
     * from the recipient.
     */
    private final CauseChannel<Task> channel;

    public final static Logger logger = LoggerFactory.getLogger(Conclusion.class);
    private final Term pattern;
    private final String rule;
    private final TimeFunctions time;
    private final boolean varIntro;
    private final int minNAL;

    public Conclusion(@NotNull Compound id, @NotNull Term pattern, @NotNull TimeFunctions time, boolean varIntro, @NotNull PremiseRule rule, CauseChannel<Task> input) {
        super(id);
        this.channel = input;
        this.pattern = pattern;
        this.time = time;
        this.varIntro = varIntro;
        this.rule = rule.toString(); //only store toString of the rule to avoid remaining attached to the RuleSet
        this.minNAL = rule.minNAL;
    }

     /**
     * @return true to allow the matcher to continue matching,
     * false to stop it
     */
    @Override
    public final boolean test(@NotNull Derivation d) {

        NAR nar = d.nar;

        nar.emotion.derivationTry.increment();

        if (minNAL > nar.level())  //HACK
            return true;

        //TODO make a variation of transform which can terminate early if exceeds a minimum budget threshold
        //  which is already determined bythe constructed term's growing complexity) in m.budget()

        Term b0 = this.pattern;
        Term b1 = compoundOrNull(d.transform(b0));
        if (b1 == null)
            return true;

        assert(b1.varPattern() == 0);

        /// ----

        d.use(Param.TTL_DERIVE_TASK_ATTEMPT);
        nar.emotion.derivationEval.increment();


        Compound c1 = compoundOrNull(b1.eval(d));
        if (c1 == null)
            return true;

        boolean polarity = true;
        if (c1.op()==NEG) {
            c1 = compoundOrNull( c1.unneg() );
            if (c1==null)
                return true;
            polarity = false;
        }



        Truth truth = d.concTruth;


        @NotNull final long[] occ;

        Compound c2;
        if (d.temporal) {

            occ = Tense.ETERNAL_RANGE.clone();

            //process time with the unnegated term
            Op o = c1.op();


            float[] confScale = {1f};

            @Nullable Compound t1 = this.time.compute(c1,
                    d, occ, confScale
            );

            //temporalization failure, could not determine temporal attributes. seems this can happen normally
            if ((t1 == null) /*|| (Math.abs(occReturn[0]) > 2047483628)*/ /* long cast here due to integer wraparound */) {
                //                 {
                //                    //FOR DEBUGGING, re-run it
                //                    Compound temporalized2 = this.time.compute(content,
                //                            m, this, occReturn, confScale
                //                    );
                //                }

//                            throw new InvalidTermException(c1.op(), c1.dt(), "temporalization failure"
//                                    //+ (Param.DEBUG ? rule : ""), c1.toArray()
//                            );

                return true;
            }

//            int tdt = t2.dt();
//            if (tdt == XTERNAL || tdt == -XTERNAL) {
//                //throw new InvalidTermException(c1.op(), c1.dt(), "XTERNAL/DTERNAL leak");
//                return true;
//            }

            //            if (Param.DEBUG && occReturn[0] != ETERNAL && Math.abs(occReturn[0] - DTERNAL) < 1000) {
            //                //temporalizer.compute(content.term(), m, this, occReturn, confScale); //leave this commented for debugging
            //                throw new NAR.InvalidTaskException(content, "temporalization resulted in suspicious occurrence time");
            //            }

            //apply any non 1.0 the confidence scale
            if (truth != null) {


                float cf = confScale[0];
                if (cf != 1) {
                    throw new UnsupportedOperationException("yet");

                    //                    truth = truth.confMultViaWeightMaxEternal(cf);
                    //                    if (truth == null) {
                    //                        throw new InvalidTaskException(content, "temporal leak");
                    //                    }
                }
            }


            if (occ[1] == ETERNAL) occ[1] = occ[0];

            c2 = t1;

        } else {
            occ = Tense.ETERNAL_RANGE;
            c2 = c1;
        }

        if (varIntro) {
            Compound Cv = normalizedOrNull(DepIndepVarIntroduction.varIntro(c2, nar), d.terms,
                    d.temporal ? d.terms.retemporalizationZero : d.terms.retemporalizationDTERNAL //select between eternal and parallel depending on the premises's temporality
            );

            if (Cv == null || Cv.equals(c2) /* keep only if it differs */)
                return true;

            c2 = Cv;
        }

        byte punc = d.concPunc;
        @Nullable ObjectBooleanPair<Compound> c3n = Task.tryContent(c2, punc, d.terms, true);
        if (c3n != null) {

            boolean negating = c3n.getTwo();

            final Compound C = c3n.getOne();


            long start = occ[0];
            long end = occ[1];
            if (start != ETERNAL && end < start) { //why?
                long s = start;
                start = end;
                end = s; //swap
            }

            float priority = d.parentPri; //d.budgeting.budget(d, C, truth, punc, start, end);
            if (priority == priority) {

                if (priority < Priority.EPSILON) {
                    return true; //wasted
                }


                if (truth!=null) {

                    if (negating)
                        polarity = !polarity;

                    if (!polarity)
                        truth = truth.negated();
                }

                short[] cause = ArrayUtils.addAll(d.parentCause, channel.id);

                DerivedTask t =
                        Param.DEBUG ?
                                new DebugDerivedTask(C, punc, truth, d, start, end, cause) :
                                new DerivedTask(C, punc, truth, d, start, end, cause);

                if (t.equals(d.task) || t.equals(d.belief)) {
                    return true; //created a duplicate of the task
                }

                t.setPri(priority);


                if (Param.DEBUG)
                    t.log(rule);

                d.accept(t);
                d.use(Param.TTL_DERIVE_TASK_SUCCESS);
            }
        }

        //        } catch (InvalidTermException | InvalidTaskException e) {
//            if (Param.DEBUG_EXTRA)
//                logger.warn("{} {}", m, e.getMessage());
//        }

        d.use(Param.TTL_DERIVE_TASK_FAIL);
        return true;
    }


}
