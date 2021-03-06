package nars.concept.builder;

import jcog.bag.Bag;
import jcog.bag.impl.CurveBag;
import jcog.list.FasterList;
import jcog.pri.PriReference;
import nars.NAR;
import nars.Op;
import nars.Param;
import nars.Task;
import nars.concept.Concept;
import nars.concept.NodeConcept;
import nars.concept.TaskConcept;
import nars.concept.dynamic.DynamicBeliefTable;
import nars.concept.dynamic.DynamicConcept;
import nars.concept.dynamic.DynamicTruthModel;
import nars.concept.state.ConceptState;
import nars.concept.state.DefaultConceptState;
import nars.table.*;
import nars.term.Compound;
import nars.term.Term;
import nars.term.Termed;
import nars.term.atom.Bool;
import nars.term.atom.Int;
import nars.term.sub.Subterms;
import nars.term.var.Variable;
import org.eclipse.collections.api.list.MutableList;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;

import static nars.Op.*;


/**
 * Created by me on 2/24/16.
 */
public class DefaultConceptBuilder implements ConceptBuilder {

    public DefaultConceptBuilder() {
        this(
                new DefaultConceptState("sleep", 64, 64, 8),
                new DefaultConceptState("awake", 64, 64, 8)
        );
    }

    public DefaultConceptBuilder(ConceptState sleep, ConceptState awake) {
        this.sleep = sleep;
        this.init = awake;
        this.awake = awake;
    }


    
    private final ConceptState init;
    
    private final ConceptState awake;
    
    private final ConceptState sleep;
    private NAR nar;

    @Override
    public Bag[] newLinkBags(Term t) {
        int v = t.volume();
        //if (/*v > 3 && */v < 16) {
//        Map sharedMap = newBagMap(v);
        Random rng = nar.random();
        Bag<Term, PriReference<Term>> termbag =
                new CurveBag<>(Param.termlinkMerge, newBagMap(v), rng, 0);
        CurveBag<PriReference<Task>> taskbag =
                new TaskLinkCurveBag(newBagMap(v), rng);

        return new Bag[] {  termbag, taskbag };
//        } else {
//            return new Bag[]{
//                    new MyDefaultHijackBag(Param.termlinkMerge),
//                    new MyDefaultHijackBag(Param.tasklinkMerge)
//            };
//        }

    }


    private TaskConcept taskConcept(final Term t) {
        DynamicTruthModel dmt = null;

        final Subterms ts = t.subterms();
        switch (t.op()) {

            case INH:

                Term subj = ts.sub(0);
                Term pred = ts.sub(1);

                Op so = subj.op();
                Op po = pred.op();

                if (dmt == null && (po.atomic || po == PROD || po.isSet())) {
                    if ((so == Op.SECTi) || (so == Op.SECTe) || (so == Op.DIFFe) || (subj instanceof Int.IntRange) || (so == PROD && subj.OR(Int.IntRange.class::isInstance))) {
                        //(P --> M), (S --> M), notSet(S), notSet(P), neqCom(S,P) |- ((S | P) --> M), (Belief:Intersection)
                        //(P --> M), (S --> M), notSet(S), notSet(P), neqCom(S,P) |- ((S & P) --> M), (Belief:Union)
                        //(P --> M), (S --> M), notSet(S), notSet(P), neqCom(S,P) |- ((P ~ S) --> M), (Belief:Difference) //extensional


                        Subterms subjsubs = subj.subterms();
                        {
                            int s = subjsubs.subs();
                            FasterList<Term> lx = new FasterList(0, new Term[s]);
                            if (subj instanceof Int.IntRange || so == PROD && subj.hasAny(INT)) {
                                Iterator<Term> iu = Int.unroll(subj);
                                if (iu!=null)
                                    iu.forEachRemaining(dsi -> lx.add(INH.the(dsi, pred)));
                                else {
                                    //??
                                }
                            }
                            if (so != PROD) {
                                for (int i = 0; i < s; i++) {
                                    Term csi = subjsubs.sub(i);
                                    //                                if (csi instanceof Int.IntRange) {
                                    //                                    //TODO??
                                    ////                                    lx.add(
                                    ////
                                    ////                                            Int.unroll(subj).forEachRemaining(dsi -> lx.add(INH.the(dsi, pred)));
                                    //                                } else {
                                    Term x = INH.the(csi, pred);
                                    assert (!(x instanceof Bool) && !(x instanceof Variable)): "(" + csi + " --> " + pred + ") produced invalid term as part of " + t;
                                    lx.add(x);
                                    //                                }
                                }
                            }


                            if (lx.size() > 1 && validDynamicSubterms(lx)) {
                                Term[] x = lx.toArrayRecycled(Term[]::new);
                                switch (so) {
                                    case INT:
                                    case PROD:
                                    case SECTi:
                                        dmt = new DynamicTruthModel.Intersection(x);
                                        break;
                                    case SECTe:
                                        dmt = new DynamicTruthModel.Union(x);
                                        break;
                                    case DIFFe:
                                        dmt = new DynamicTruthModel.Difference(x[0], x[1]);
                                        break;
                                }
                            }
                        }


                    } /*else if (po.image) {
                        Compound img = (Compound) pred;
                        Term[] ee;

                        int relation = img.dt();
                        if (relation != DTERNAL) {
                            int s = img.size();
                            ee = new Term[s];

                            for (int j = 1, i = 0; i < s; ) {
                                if (j == relation)
                                    ee[i++] = subj;
                                if (i < s)
                                    ee[i++] = img.sub(j++);
                            }
                        } else {
                            ee = t.toArray();
                        }
                        Compound b = compoundOrNull(INH.the(DTERNAL, $.p(ee), img.sub(0)));
                        if (b != null)
                            dmt = new DynamicTruthModel.Identity(t, b);
                    }*/

                }

                if (dmt == null && (so.atomic || so == PROD || so.isSet())) {
                    if ((po == Op.SECTi) || (po == Op.SECTe) || (po == DIFFi)) {
                        //(M --> P), (M --> S), notSet(S), notSet(P), neqCom(S,P) |- (M --> (P & S)), (Belief:Intersection)
                        //(M --> P), (M --> S), notSet(S), notSet(P), neqCom(S,P) |- (M --> (P | S)), (Belief:Union)
                        //(M --> P), (M --> S), notSet(S), notSet(P), neqCom(S,P) |- (M --> (P - S)), (Belief:Difference) //intensional
                        Compound cpred = (Compound) pred;
                        {
                            int s = cpred.subs();
                            //TODO use a List for unrolling IntRange's
                            Term[] x = new Term[s];
                            boolean valid = true;
                            for (int i = 0; i < s; i++) {
                                Term y;
                                if (!validDynamicSubterm.test(y = INH.the(subj, cpred.sub(i)))) {
                                    valid = false;
                                    break;
                                }
                                x[i] = y;
                            }

                            if (valid) {
                                switch (po) {
                                    case SECTi:
                                        dmt = new DynamicTruthModel.Union(x);
                                        break;
                                    case SECTe:
                                        dmt = new DynamicTruthModel.Intersection(x);
                                        break;
                                    case DIFFi:
                                        dmt = new DynamicTruthModel.Difference(x[0], x[1]);
                                        break;
                                }
                            }
                        }
                    } /*else if (so.image) {
                        Compound img = (Compound) subj;
                        Term[] ee = new Term[img.size()];

                        int relation = img.dt();
                        int s = ee.length;
                        for (int j = 1, i = 0; i < s; ) {
                            if (j == relation)
                                ee[i++] = pred;
                            if (i < s)
                                ee[i++] = img.sub(j++);
                        }
                        Compound b = compoundOrNull(INH.the(DTERNAL, img.sub(0), $.p(ee)));
                        if (b != null)
                            dmt = new DynamicTruthModel.Identity(t, b);
                    }*/

                }

                break;

            case CONJ:
                //allow variables onlyif they are not themselves direct subterms of this
                if (validDynamicSubterms(ts)) {
                    dmt = DynamicTruthModel.Intersection.conj;
                }
                break;

            case DIFFe:
                //root DIFFe (not subj or pred of an inh)
                if (validDynamicSubterms(ts))
                    dmt = new DynamicTruthModel.Difference(ts.arrayShared());
                break;

            case NEG:
                throw new RuntimeException("negation terms can not be conceptualized as something separate from that which they negate");
        }

        if (dmt != null) {

            return new DynamicConcept(t,
                    new DynamicBeliefTable(t, newTemporalBeliefTable(t), dmt, true),
                    goalable(t) ?
                        new DynamicBeliefTable(t, newTemporalBeliefTable(t), dmt, false) :
                        BeliefTable.Empty,
                    this);

        } else {
            return new TaskConcept(t, this);
        }
    }

    @Override
    public BeliefTable beliefTable(Term c, boolean beliefOrGoal) {
        //TemporalBeliefTable newTemporalTable(final int tCap, NAR nar) {
        //return new HijackTemporalBeliefTable(tCap);
        //return new RTreeBeliefTable(tCap);
        if (!c.hasAny(Op.VAR_QUERY) && (beliefOrGoal ? c.op().beliefable : goalable(c))) {
            return new DefaultBeliefTable(newTemporalBeliefTable(c));
        } else {
            return BeliefTable.Empty;
        }
    }

    @Override
    public TemporalBeliefTable newTemporalBeliefTable(Term c) {
//        if (c.complexity() < 12) {
        return new RTreeBeliefTable();
        //c.complexity() < 6 ? new DisruptorBlockingQueue() : new LinkedBlockingQueue<>()/
//        } else {
//            return new HijackTemporalBeliefTable();
//        }
    }


    @Override
    public QuestionTable questionTable(Term term, boolean questionOrQuest) {
        Op o = term.op();
        if (questionOrQuest ? o.beliefable : o.goalable) {
            //return new HijackQuestionTable(0, 4);
            return new QuestionTable.DefaultQuestionTable();
        } else {
            return QuestionTable.Empty;
        }
    }


    final static Predicate<Term> validDynamicSubterm = x ->
            x!=null && Task.validTaskTerm(x.unneg());

    private static boolean validDynamicSubterms(Subterms subterms) {
        return subterms.AND(validDynamicSubterm);
    }

    private static boolean validDynamicSubterms(MutableList<Term> subterms) {
        return subterms.allSatisfy(DefaultConceptBuilder.validDynamicSubterm::test);
    }

    @Override
    public void start( NAR nar) {
        this.nar = nar;
    }


    @Override
    public Termed build(Term t) {
        Concept c;
        if (Task.validTaskTerm(t))
            c = taskConcept(t);
        else
            c = new NodeConcept(t, newLinkBags(t));

        return c;
    }

    @Override
    public ConceptState init() {
        return init;
    }
    
    @Override
    public ConceptState awake() {
        return awake;
    }

    
    @Override
    public ConceptState sleep() {
        return sleep;
    }

    
    public static Map newBagMap(int volume) {
        //int defaultInitialCap = 0;
        float loadFactor = 0.75f;

//        if (concurrent()) {
////            //return new ConcurrentHashMap(defaultInitialCap, 1f);
////            //return new NonBlockingHashMap(cap);
////            return new org.eclipse.collections.impl.map.mutable.ConcurrentHashMapUnsafe<>();
////            //ConcurrentHashMapUnsafe(cap);
////        } else {
////            return new HashMap(defaultInitialCap, 1f);
//            //   if (volume < 16) {
//            return new ConcurrentHashMap(0, loadFactor);
//
////            } else if (volume < 32) {
////                return new SynchronizedHashMap(0, loadFactor);
////                //return new TrieMap();
////            } else {
////                return new SynchronizedUnifiedMap(0, loadFactor);
////            }
//        } else {
            //return new UnifiedMap(0, loadFactor);
            return new HashMap(0, loadFactor);
//        }

    }

    public boolean concurrent() {
        return nar.exe.concurrent();
    }

//    private class MyDefaultHijackBag extends DefaultHijackBag {
//        public MyDefaultHijackBag(PriMerge merge) {
//            super(merge, 0, 5);
//        }
//
//        @Override
//        protected Random random() {
//            return nar.random();
//        }
//    }
}
