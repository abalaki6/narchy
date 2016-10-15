package nars.nal.meta;

import nars.Op;
import nars.truth.Truth;
import nars.truth.func.TruthOperator;
import org.jetbrains.annotations.NotNull;

import static nars.Symbols.*;

/**
 * Evaluates the truth of a premise
 */
abstract public class Solve extends AtomicBoolCondition {

    private final transient String id;

    public final Conclude conclude;

    public final TruthOperator belief;
    public final TruthOperator desire;

    public Solve(String id, Conclude conclude, TruthOperator belief, TruthOperator desire) {
        super();
        this.id = id;
        this.conclude = conclude;
        this.belief = belief;
        this.desire = desire;
    }

    @NotNull
    @Override
    public String toString() {
        return id;
    }

    final boolean measure(@NotNull PremiseEval m, char punct) {



        switch (punct) {
            case BELIEF:
            case GOAL:
                TruthOperator tf = (punct == BELIEF) ? belief : desire;
                if (tf == null)
                    return false; //there isnt a truth function for this punctuation

                boolean single = tf.single();

                if (!tf.allowOverlap() && m.overlap(single))
                    return false;

                break;
            case QUESTION:
            case QUEST:
                //a truth function so check cyclicity
                if (m.cyclic)
                    return false;
                break;
            default:
                throw new Op.InvalidPunctuationException(punct);
        }

        TruthOperator f;
        if (punct == BELIEF)
            f = belief;
        else if (punct == GOAL)
            f = desire;
        else
            f = null;



        boolean single =  f == null || f.single();

        Truth t;
        if (f == null) {
            t = null;
        } else {

            //task truth is not involved in the outcome of this; set task truth to be null to prevent any negations below:
            Truth taskTruth = m.taskTruth;

            //truth function is single premise so set belief truth to be null to prevent any negations below:

            Truth beliefTruth = (single) ? null : m.beliefTruth;

            t = f.apply(
                    taskTruth,
                    beliefTruth,
                    m.nar,
                    m.confMin
            );
            if (t == null)
                return false;
        }

        return m.setPunct(t, punct, m.evidence(single));
    }


}

