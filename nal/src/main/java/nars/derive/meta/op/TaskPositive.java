package nars.derive.meta.op;

import nars.derive.meta.AtomicBoolCondition;
import nars.premise.Derivation;
import nars.truth.Truth;
import org.jetbrains.annotations.NotNull;

/** task truth is postiive */
public class TaskPositive extends AtomicBoolCondition {

    public static final TaskPositive the = new TaskPositive();

    @Override
    public boolean run(@NotNull Derivation m, int now) {
        Truth t = m.premise.task.truth();
        return (t!=null && t.freq() >= 0.5f);
    }

    @NotNull
    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

}