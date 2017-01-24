package nars.budget;

import jcog.Util;
import nars.NAR;
import nars.Param;
import org.jetbrains.annotations.NotNull;

import static nars.budget.Budget.aveGeo;

//import nars.data.BudgetedStruct;

/**
 * Created by jim on 1/6/2016.
 */
public interface Budgeted  {

    static float priSum(@NotNull Iterable<? extends Budgeted> c) {
        float totalPriority = 0;
        for (Budgeted i : c)
            totalPriority += i.priSafe(0);
        return totalPriority;
    }

    static void normalizePriSum(@NotNull Iterable<? extends Budgeted> l, float total) {

        float priSum = Budgeted.priSum(l);
        float mult = total / priSum;
        for (Budgeted b : l) {
            b.budget().priMult(mult);
        }

    }

    /**
     * randomly selects an item from a collection, weighted by priority
     */
    static <E extends Budgeted> E selectRandomByPriority(@NotNull NAR memory, @NotNull Iterable<E> c) {
        float totalPriority = priSum(c);

        if (totalPriority == 0) return null;

        float r = memory.random.nextFloat() * totalPriority;

        E s = null;
        for (E i : c) {
            s = i;
            r -= s.pri();
            if (r < 0)
                return s;
        }

        return s;

    }


    /** the result of this should be that pri() is not finite (ex: NaN)
     * returns false if already deleted (allowing overriding subclasses to know if they shold also delete) */
    boolean delete();

    default boolean delete(Object ignored) { return delete(); }

    @NotNull
    Budget budget();

    /**
     * To summarize a BudgetValue into a single number in [0, 1]
     *
     * @return The summary value
     */
    default float summary() {
        return aveGeo(pri(), 0, qua());
    }

    float pri();


    default float priSafe(float valueIfInactive) {
        float p = pri();
        return p == p ? p : valueIfInactive;
    }

    boolean isDeleted();

    float qua();



    default boolean equalsBudget(@NotNull Budgeted t, float epsilon) {
        return Util.equals(pri(), t.pri(), epsilon) &&
                Util.equals(qua(), t.qua(), epsilon);
    }

    default boolean equalsBudget(@NotNull Budgeted b) {
        return equalsBudget(b, Param.BUDGET_EPSILON);
    }


}
