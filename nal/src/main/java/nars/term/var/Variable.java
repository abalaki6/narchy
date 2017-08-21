package nars.term.var;

import nars.Op;
import nars.term.Term;
import nars.term.atom.Atomic;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

import static nars.Op.Null;

/**
 * similar to a plain atom, but applies altered operating semantics according to the specific
 * varible type, as well as serving as something like the "marker interfaces" of Atomic, Compound, ..
 *
 * implemented by both raw variable terms and variable concepts
 **/
public interface Variable extends Atomic {


    /** an ID by which this variable can be uniquely identified,
     * among the other existing variables with the same ID but
     * from other variable op's #$?%
     */
    int id();

    @Override
    int hashCode();


    @Override
    boolean equals(Object o);

    //    @Override
//    default int volume() {
//        //TODO decide if this is the case for zero-or-more ellipsis
//        return 1;
//    }


    @Override
    @Nullable
    default Term normalize() {
        return this; //override: only normalize if given explicit offset with normalize(int offset) as is done during normalization
    }

    @Override
    Variable normalize(int offset);

    /**
     * The syntactic complexity of a variable is 0, because it does not refer to
     * any concept.
     *
     * @return The complexity of the term, an integer
     */
    @Override
    default int complexity() {
        return 0;
    }

    @Override
    @NotNull
    default Term conceptual() {
        return Null;
    }

    @Override
    @Nullable
    default Set<Term> varsUnique(@Nullable Op type, @NotNull Set<Term> exceptIfHere) {
        if (((type == null || op() == type)) && !exceptIfHere.contains(this))
            return Set.of(this);
        else
            return null;
    }

    @Override
    default void init(@NotNull int[] meta) {
        int i;
        switch (op()) {
            case VAR_DEP:
                i = 0;
                break;
            case VAR_INDEP:
                i = 1;
                break;
            case VAR_QUERY:
                i = 2;
                break;
            case VAR_PATTERN:
                i = 3;
                break;
            default:
                throw new UnsupportedOperationException();
        }
        meta[i] ++;
        meta[4] ++;
        meta[5] |= structure();
    }

}
