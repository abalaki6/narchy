package nars.op.mental;

import com.sun.deploy.util.ArrayUtil;
import nars.*;
import nars.bag.impl.CurveBag;
import nars.budget.Activation;
import nars.budget.Budget;
import nars.budget.merge.BudgetMerge;
import nars.budget.policy.ConceptPolicy;
import nars.concept.AtomConcept;
import nars.concept.CompoundConcept;
import nars.concept.Concept;
import nars.concept.PermanentConcept;
import nars.link.BLink;
import nars.link.DefaultBLink;
import nars.op.MutaTaskBag;
import nars.table.BeliefTable;
import nars.table.QuestionTable;
import nars.task.GeneratedTask;
import nars.task.MutableTask;
import nars.term.Compound;
import nars.term.Term;
import nars.term.container.TermContainer;
import nars.term.container.TermVector;
import nars.term.subst.FindSubst;
import nars.util.data.MutableInteger;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static nars.nal.UtilityFunctions.or;
import static nars.time.Tense.ETERNAL;
import static nars.util.Util.unitize;

/**
 * compound<->dynamic atom abbreviation.
 *
 * @param S serial term type
 */
public class Abbreviation/*<S extends Term>*/ extends MutaTaskBag<BLink<Compound>> {

    static final Logger logger = LoggerFactory.getLogger(Abbreviation.class);

    private static final AtomicInteger currentTermSerial = new AtomicInteger(0);

    /** when a concept is important and exceeds a syntactic complexity above
     * this value multiplied by the NAR's volume limit, then LET NARS NAME IT. */
    public final MutableInteger minAbbreviableVolume = new MutableInteger();

    //TODO different parameters for priorities and budgets of both the abbreviation process and the resulting abbreviation judgment
    //public AtomicDouble priorityFactor = new AtomicDouble(1.0);
    /**
     * generated abbreviation belief's confidence
     */
    public final MutableFloat abbreviationConfidence;

    /** abbreviations per processed task */
    public final MutableFloat abbreviationProbability = new MutableFloat(2f);

    /** common evidence for all AbbreviationTasks generated by this plugin, to prevent deriving their overlaps */
    final long abbreviationEvidence;

    @NotNull
    protected final NAR nar;
    private final String termPrefix;



    public Abbreviation(@NotNull NAR n, String termPrefix, int volMin, float selectionRate, int capacity) {
        super(selectionRate, new CurveBag<>(BudgetMerge.plusBlend, n.random), n);

        this.nar = n;
        this.termPrefix = termPrefix;
        this.bag.setCapacity(capacity);
        this.minAbbreviableVolume.set(volMin);
        this.abbreviationConfidence = new MutableFloat(nar.confidenceDefault(Symbols.BELIEF));
        this.abbreviationEvidence = n.clock.nextStamp();
    }

    @Nullable
    @Override
    protected BLink<Compound> filter(Task task) {

        if (task instanceof AbbreviationTask) //avoids feedback
            return null;

        Term t = task.term();
        int vol = t.volume();
        int minVol = this.minAbbreviableVolume.intValue();
        if (vol >= minVol) {

            float score;
            if ((score = scoreIfExceeds(task, nar.random.nextFloat())) > 0) {

                score *= (1f / (1f + Math.max(0, (t.complexity() - minVol)))); //decrease score by any additional complexity above the volume threshold
                @NotNull Budget b = task.budget();
                return new DefaultBLink(t,
                        score,
                        b.dur(),
                        b.qua());
            }
        }

        return null;
    }


    @Override
    protected void accept(BLink<Compound> b) {

        Term term = b.get();
        Concept abbreviable = nar.concept(term);
        if ((abbreviable != null) &&
            !(abbreviable instanceof PermanentConcept) &&
            !(abbreviable instanceof AliasConcept) &&
            (abbreviable.get(Concept.Savior.class) == null)) {

            abbreviate((CompoundConcept)abbreviable, b);
        }

    }

    @Nullable
    static Compound newRelation(@NotNull Concept abbreviated, @NotNull String id) {
        return
                (Compound) $.sim(abbreviated.term(), $.the(id));
                //(Compound) $.equi(abbreviated.term(), id);
                //(Compound) $.secte(abbreviated.term(), id);

        //old 1.6 pattern:
        //Operation compound = Operation.make(
        //    Product.make(termArray(termAbbreviating)), abbreviate);*/
    }

    @NotNull
    protected String newSerialTerm() {

        return (termPrefix + "_" + Integer.toString(currentTermSerial.incrementAndGet(), 36));

//        int olen = name.length();
//        switch (olen) {
//            case 0:
//                throw new RuntimeException("empty atom name: " + name);
//
////            //re-use short term names
////            case 1:
////            case 2:
////                return theCached(name);
//
//            default:
//                if (olen > Short.MAX_VALUE/2)
//                    throw new RuntimeException("atom name too long");

        //  }
    }

    private float scoreIfExceeds(Task task, float min) {
        float s = or(task.priIfFiniteElseZero(), task.qua());
        if (s >= min) {
            s *= abbreviationProbability.floatValue();
            if (s >= min) {

                //higher probability for terms nearer the thresh. smaller and larger get less chance
//                s *= 1f - unitize(
//                        Math.abs(task.volume() - volThresh) /
//                                (threshFalloffRate) );

                if (s >= min)
                    return s;
            }
        }
        return -1;
    }

    protected void abbreviate(@NotNull CompoundConcept abbreviated, Budget b) {
        //Concept abbreviation = nar.activate(, NewAbbreviationBudget);

        String id = newSerialTerm();


        Compound abbreviation = newRelation(abbreviated, id);
        if (abbreviation != null) {

            Compound abbreviatedTerm = abbreviated.term();

            AliasConcept alias = new AliasConcept(id, abbreviated, nar);
            nar.concepts.set(alias, alias);
            nar.concepts.set(abbreviatedTerm, alias); //override the abbreviated on lookups
            logger.info("{} <=== {}", alias, abbreviatedTerm);


            //abbreviated.put(Abbreviation.class, alias); //abbreviated by the serial

            //logger.info("Abbreviation {}", abbreviation);


//            AbbreviationTask t = new AbbreviationTask(
//                    abbreviation, abbreviatedTerm, alias, abbreviationConfidence.floatValue(), abbreviationEvidence);
//            t.time(nar.time(), ETERNAL);
//            t.setBudget(b);
//            t.log("Abbreviate");
//
//            nar.inputLater( t );
//            logger.info("new: {}", t);


        }
    }

    static final class AliasConcept extends AtomConcept {

        private final Concept abbr;
        private TermContainer templates;

        public AliasConcept(@NotNull String term, @NotNull Concept abbreviated, @NotNull NAR nar) {
            super(term, Op.ATOM, abbreviated.termlinks(), abbreviated.tasklinks());

            abbreviated.put(Concept.Savior.class, this);

            this.abbr = abbreviated;
            this.templates = TermVector.the(ArrayUtils.add(abbreviated.templates().terms(), abbreviated.term()));
            rewriteLinks(nar);
        }


        /** rewrite termlinks and tasklinks which contain the abbreviated term...
         *      (but are not equal to since tasks can not have atom content)
         *  ...replacing it with this alias */
        private void rewriteLinks(@NotNull NAR nar) {
            Term that = abbr.term();
            termlinks().compute(existingLink -> {
               Term x = existingLink.get();
               Term y = nar.concepts.replace(x, that, this);
               return (y!=null && y != x) ?
                       termlinks().newLink(y, existingLink) :
                       existingLink;
            });
            tasklinks().compute(existingLink -> {
                Task xt = existingLink.get();
                Term x = xt.term();

                if (!x.equals(that) && !x.hasTemporal()) {
                    Term y = $.terms.replace(x, that, this);
                    if ( y != x && y instanceof Compound) {
                        Task yt = MutableTask.clone(xt, (Compound) y, nar);
                        if (yt!=null)
                            return termlinks().newLink(yt, existingLink);
                    }
                }

                return existingLink;

            });
        }


        @Override
        public void delete(NAR nar) {
            abbr.delete(nar);
            super.delete(nar);
        }

        @Override
        public @Nullable TermContainer templates() {
            return templates;
        }

        @Override
        public ConceptPolicy policy() {
            return abbr.policy();
        }

        @Override
        public void policy(@NotNull ConceptPolicy p, long now, List<Task> removed) {
            abbr.policy(p, now, removed);
        }

        /** equality will have already been tested here, and the parent super.unify() method is just return false. so skip it and just try the abbreviated */
        @Override public boolean unify(@NotNull Term y, @NotNull FindSubst subst) {
            return /*super.unify(y, subst) || */abbr.term().unify(y, subst);
        }

        @Override
        public boolean equals(Object u) {
            return super.equals(u);
        }

        @Override
        public final Activation process(@NotNull Task input, NAR nar) {
            return abbr.process(input, nar);
        }

        @Override
        public @Nullable Map<Object, Object> meta() {
            return abbr.meta();
        }

        @NotNull @Override public BeliefTable beliefs() {  return abbr.beliefs();        }
        @NotNull @Override public BeliefTable goals() { return abbr.goals();         }
        @NotNull @Override public QuestionTable questions() { return abbr.questions();         }
        @NotNull @Override public QuestionTable quests() { return abbr.quests(); }
    }


    static class AbbreviationTask extends GeneratedTask {

        private
        @NotNull
        final Compound abbreviated;
        private final AliasConcept alias;

        public AbbreviationTask(Compound abbreviation, @NotNull Compound abbreviated, AliasConcept alias, float conf, long abbreviationEvidence) {
            super(abbreviation, Symbols.BELIEF, $.t(1, conf));
            setEvidence(abbreviationEvidence);
            this.abbreviated = abbreviated;
            this.alias = alias;
        }


    }
}
