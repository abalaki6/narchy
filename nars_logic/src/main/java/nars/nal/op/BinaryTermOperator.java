package nars.nal.op;

import nars.term.Term;
import nars.term.TermBuilder;
import nars.term.compound.Compound;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by me on 12/12/15.
 */
public abstract class BinaryTermOperator extends ImmediateTermTransform {

    @Override public final Term function(@NotNull Compound x, TermBuilder i) {
        if (x.size()<2)
            throw new RuntimeException(this + " requires >= 2 args");

        return apply(x.term(0), x.term(1), i);
    }

    @Nullable
    public abstract Term apply(Term a, Term b, TermBuilder i);
}
