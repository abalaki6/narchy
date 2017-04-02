package nars;

import jcog.bag.PLink;
import nars.concept.Concept;
import nars.term.Termed;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Iterator;
import java.util.function.Predicate;

/**
 * Created by me on 12/27/16.
 */
public interface Focus extends Iterable<PLink<Concept>> {

    /**
     * if the concept is active, returns the Concept while applying the boost factor to its budget
     */

    void activate(Concept term, float priToAdd);


    /**
     * @return current priority of the concept, or NaN if concept isnt active
     */
    float pri(@NotNull Termed concept);

    default float pri(@NotNull Termed concept, float valueIfInactive) {
        float p = pri(concept);
        return (p == p) ? p : valueIfInactive;
    }

    Iterable<PLink<Concept>> conceptsActive();

    @NotNull
    @Override
    default Iterator<PLink<Concept>> iterator() {
        return conceptsActive().iterator();
    }

    void sample(int max, Predicate<? super PLink<Concept>> c);


    Focus NULL_FOCUS = new Focus() {
        @Override
        public void activate(Concept term, float priToAdd) {

        }

        @Override
        public void sample(int max, Predicate<? super PLink<Concept>> c) {

        }

        @Override
        public float pri(@NotNull Termed concept) {
            return 0;
        }

        @Override
        public Iterable<PLink<Concept>> conceptsActive() {
            return Collections.emptyList();
        }
    };


}