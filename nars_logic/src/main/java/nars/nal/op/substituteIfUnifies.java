package nars.nal.op;

import nars.Op;
import nars.nal.meta.PremiseMatch;
import nars.term.Term;
import nars.term.compound.Compound;
import nars.term.transform.FindSubst;
import nars.util.data.random.XorShift128PlusRandom;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

/** substituteIfUnifies(term, variableType, varFrom, varTo) */
public final class substituteIfUnifies extends substitute {



    @Nullable
    @Override public Term function(@NotNull Compound p, @NotNull PremiseMatch r) {
        final Term[] xx = p.terms();
        final Term term = xx[0];
        final Term op = xx[1];
        final Term x = xx[2];
        final Term y = xx[3];

        FindSubst umap = unifies(op, x, y);
        if (umap!=null) {
            //umap.putXY(term, y);
            return subst(r, umap, term);
        }

        return term;
    }

    static FindSubst unifies(@NotNull Term op, @NotNull Term x, @NotNull Term y) {
        Op o = getOp(op);
        Random rng = new XorShift128PlusRandom(1);
        FindSubst sub = new FindSubst(o, rng) {
            @Override public boolean onMatch() {
                //should not get called by .match only
                return false;
            }
        };

        if (sub.match(x, y)) {
            return sub;
        }

        return null;
    }
}
