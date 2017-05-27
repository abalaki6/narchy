package nars.attention;

import jcog.bag.Bag;
import jcog.list.FasterList;
import jcog.map.SaneObjectFloatHashMap;
import jcog.pri.PLink;
import jcog.pri.Priority;
import jcog.pri.RawPLink;
import nars.NAR;
import nars.Task;
import nars.budget.PLinkUntilDeleted;
import nars.concept.AtomConcept;
import nars.concept.Concept;
import nars.concept.TaskConcept;
import nars.control.ConceptFire;
import nars.task.ITask;
import nars.task.TruthPolation;
import nars.task.UnaryTask;
import nars.term.Compound;
import nars.term.Term;
import nars.term.Termed;
import nars.term.container.TermContainer;
import nars.term.var.Variable;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.eclipse.collections.api.block.procedure.primitive.ObjectFloatProcedure;
import org.eclipse.collections.impl.map.mutable.primitive.ObjectFloatHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static jcog.math.Interval.unionLength;
import static nars.Op.VAR_QUERY;
import static nars.time.Tense.ETERNAL;

/**
 * activation from a point source to its subterm components (termlink templates)
 */
public class SpreadingActivation extends UnaryTask<Task> implements ObjectFloatProcedure<Termed> {

    static final ThreadLocal<ObjectFloatHashMap<Termed>> activationsSmall =
            ThreadLocal.withInitial(() -> new SaneObjectFloatHashMap<>(16));

    static final ThreadLocal<ObjectFloatHashMap<Termed>> activationsLarge =
            ThreadLocal.withInitial(() -> new SaneObjectFloatHashMap<>(64));

    @Deprecated
    private int maxDepth; //TODO subtract from this then it wont need stored


    transient final protected TaskConcept origin;

    final MutableFloat linkOverflow = new MutableFloat(0);

    /**
     * cached for fast access
     */
    transient int dur;

    /**
     * momentum > 0.5 means parents preserve more of the priority than sharing with children
     */
    float momentum;

    /**
     * values closer to zero mean atom tasklink activation is less filtered by time;
     * values closer to one mean atom tasklink activation is more filtered to specific time
     */
    private static final float temporalSpecificity = 0.5f;

    /**
     * 0.5 = forward/backward termlinking is balanced.
     * > 0.5 = more towards the forward link (ie, origin -> subterms)
     * < 0.5 = more towards the backward link (ie, subterms -> origin)
     */
    private static final float TERMLINK_BALANCE = 0.5f;

    transient private ObjectFloatHashMap<Termed> spread;
    transient private NAR nar;
    transient private FasterList<ITask> activations;

    /** priority of the task, which will differ from the priority of the activation.
     * we want to activate with the task's current priority regardless how low the priority of this
     * activation becomes.*/
    transient private float taskPri;


    /**
     * runs the task activation procedure
     */
    public SpreadingActivation(@NotNull Task t, @NotNull TaskConcept c) {
        super(t, t.priSafe(0));
        this.origin = c;
    }

    @Override
    public ITask[] run(@NotNull NAR nar) {

        this.taskPri = id.pri();
        if (taskPri!=taskPri)
            return null;


        this.momentum = nar.momentum.floatValue();
        this.dur = nar.dur();
        this.nar = nar;

        this.maxDepth = levels(origin);

        ITask[] a = null;

        /* HEURISTIC estimate */
        spread = (origin.volume() > 16 ? activationsLarge : activationsSmall).get();
        spread.clear();
        //assert(spread.isEmpty());

        int ss = 0;
        try {

            link(origin, 1f, 0);
            nar.emotion.stress(linkOverflow);

            ss = spread.size();
            if (ss > 0) {
                this.activations = new FasterList<>(0, a = new ITask[ss]);
                this.spread.forEachKeyValue(this);
            }

        } finally {
            if (ss > 0) {
                this.spread.clear(); //makes sure it clear

                //GC relief
                this.spread = null;
                this.activations = null;
                this.nar = null;
            }
        }

        return a;
    }

    public static int levels(@NotNull Compound host) {
        switch (host.op()) {

            case PROD:

            case SETe:
            case SETi:

            case IMGe:
            case IMGi:
                return 1;

            case DIFFe:
            case DIFFi:
            case SECTi:
            case SECTe:
                return 1;

            case SIM:
                return 2;

            case INH:
                return 3;

            case EQUI:
                return 3;

            case IMPL:
                return 3;

            case CONJ:
                return 3;

//                int s = host.size();
//                if (s <= Param.MAX_CONJ_SIZE_FOR_LAYER2_TEMPLATES) {
//                    int vars = host.vars();
//                    return (vars > 0) ? 3 : 2;
//                } else {
//                    return 2;
//                    //return (vars > 0) ? 2 : 1; //prevent long conjunctions from creating excessive templates
//                }

            default:
                throw new UnsupportedOperationException("unhandled operator type: " + host.op());
        }
    }

    @Override
    public void value(@NotNull Termed c, float scale) {
        //System.out.println("\t" + k + " " + v);

        float p = taskPri;
        float pScaled = p * scale;
        if (pScaled >= Priority.EPSILON) {
            if (c instanceof Concept)
                activations.add(new ConceptFire((Concept) c, pScaled));
        }

        if (pScaled >= Priority.EPSILON) {


            termBidi(c, p * TERMLINK_BALANCE, p * (1f - TERMLINK_BALANCE), scale);

            if (c instanceof Concept) {
                tasklink((Concept) c, p * scale);
                //            if (c instanceof AtomConcept) {
                //                activateAtom((AtomConcept) c, scale);
                //            }
            }
        }

    }

    @Nullable
    void link(@NotNull Termed target, float scale, int depth) {

//        if (scale < Pri.EPSILON)
//            return;

        if ((target instanceof Variable)) {
            if (target.op() == VAR_QUERY)
                return; //dont create termlinks to query variable subterms
        } else {
            @Nullable Concept termConcept = nar.conceptualize(target);
            if (termConcept != null)
                target = termConcept;
        }

        float parentActivation = scale;

        if (depth < maxDepth) {


            //recurse
            TermContainer t =
                    target instanceof Concept ?
                            ((Concept) target).templates() : //allow concept to override its templates
                            target instanceof Compound ? ((Compound) target).subterms() : null;

            if (t != null && t.size() > 0)
                parentActivation = linkSubterms(t, scale, depth + 1);


        } /*else {
            parentActivation = scale;
        }*/

        //assert (target.op() != NEG); //should have been un-negated already

        if (parentActivation > 0)
            spread.addToValue(target, parentActivation);

    }

    protected float activateAtom(AtomConcept atom, float scale) {

        Bag<Task, PLink<Task>> tlinks = atom.tasklinks();
        int n = tlinks.size();
        if (n > 0) {

            //initially start with a fair budget assuming each link receives a full share
            float subActivation = ((1f - momentum) * scale) / (n);
            //final float[] change = {0};

            long inStart = id.start();
            long inEnd = id.end();

            float[] additionalPressure = {0};
            tlinks.forEach((b) -> {
                float subSubActivation = subActivation;/// * b.qua();

                if (inStart != ETERNAL) {
                    Task bt = b.get();
                    long bs = bt.start();
                    if (bs != ETERNAL) {
                        //Temporal vs. temporal: reduce subActivation by temporal distance

                        long be = bt.end();
                        long timeDistance = Math.max(0,
                                unionLength(inStart, inEnd, bs, be)
                                        - (inEnd - inStart) //task range
                                        - (be - bs)  //belief range
                                        - dur
                        ); //perceptual duration

                        //multiply by temporal relevancy
                        subSubActivation = subSubActivation * (
                                (1f - temporalSpecificity) + //min reduction
                                        (temporalSpecificity) * TruthPolation.evidenceDecay(
                                                1f,
                                                (int) Math.ceil(dur),
                                                timeDistance));

                    }
                }

                if (subSubActivation >= Priority.EPSILON) {
                    subSubActivation -= b.priAddOverflow(subSubActivation, additionalPressure); //activate the link
                }

                //change[0] += (subActivation - subSubActivation);
            });
            if (additionalPressure[0] > 0) {
                tlinks.pressurize(additionalPressure[0]);
            }

            //recoup losses to the parent
            //parentActivation += change[0];
            float parentActivation = (momentum * scale);
            return parentActivation;


        } else {
            //termlinks?
        }

        return scale;
    }

    protected float linkSubterms(@NotNull TermContainer targetSubs, float scale, int nextDepth) {

        int n = targetSubs.size();
        if (n > 0) {
            float childScale = ((1f - momentum) * scale) / (n);

            for (int i = 0; i < n; i++) {
                link(targetSubs.sub(i).unneg(), childScale, nextDepth); //link and recurse to the concept
            }
            float parentActivation = scale * momentum;
            return parentActivation;

        }

        return scale;
    }


    final void termBidi(@NotNull Termed rcpt, float tlForward, float tlReverse, float scale) {

        if (rcpt == this.origin)
            return;

        Term rcptTerm = rcpt.term();

        if (tlForward > 0)
            termlink(origin, rcptTerm, tlForward * scale);

        if (rcpt instanceof Concept && tlReverse > 0)
            termlink((Concept) rcpt, origin, tlReverse * scale);

    }

    void tasklink(Concept rcpt, float pri) {

        rcpt.tasklinks().put(
                //new RawPLink(value, pri),
                new PLinkUntilDeleted(id, pri),
                null);

    }

    void termlink(Concept recipient, Term target, float pri) {
        recipient.termlinks().put(new RawPLink(target, pri), linkOverflow);
    }


}


            /*else {
                if (Param.ACTIVATE_TERMLINKS_IF_NO_TEMPLATE) {
                    Bag<Term> bbb = targetConcept.termlinks();
                    n = bbb.size();
                    if (n > 0) {
                        float subScale1 = subScale / n;
                        if (subScale1 >= minScale) {
                            bbb.forEachKey(x -> {
                                //only activate:
                                linkSubterm(x, subScale1, depth + 1); //Link the peer termlink bidirectionally
                            });
                        }
                    }
                }
            }*/