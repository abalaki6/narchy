package nars.derive.meta;

import nars.premise.Derivation;
import nars.truth.func.TruthOperator;
import org.jetbrains.annotations.NotNull;

/**
 * Created by me on 5/26/16.
 */
public final class SolvePuncFromTask extends Solve {

    public SolvePuncFromTask(String i, Conclude der, TruthOperator belief, TruthOperator desire, boolean beliefProjected) {
        super(i, der, belief, desire, beliefProjected);
    }

    @Override
    public boolean test(@NotNull Derivation m) {
        return measure(m, m.taskPunct);
    }
}
