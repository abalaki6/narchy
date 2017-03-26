package nars.derive.meta;

import nars.derive.meta.op.AbstractPatternOp.PatternOp;
import nars.premise.Derivation;
import nars.term.atom.Atom;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Created by me on 5/21/16.
 */
public final class PatternOpSwitch extends Atom /* TODO represent as some GenericCompound */ implements BoolPredicate<Derivation> {

    public final BoolPredicate[] proc = new BoolPredicate[32]; //should be large enough
    public final int subterm;


    public PatternOpSwitch(int subterm, @NotNull Map<PatternOp, BoolPredicate> cases) {
        super('"' + cases.toString() + '"');

        this.subterm = subterm;

        cases.forEach((c,p) -> proc[c.opOrdinal] = p);
    }

    @Override
    public boolean test(@NotNull Derivation m) {
        BoolPredicate p = proc[m.subOp(subterm)];
        if (p!=null) {
            p.test(m);
        }
        return true;
    }
}
