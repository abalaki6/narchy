package nars.table;

import nars.Task;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

/**
 * holds a set of ranked question/quests tasks
 * top ranking items are stored in the lower indexes so they will be first iterated
 */
public interface TaskTable extends Iterable<Task> {

    @Deprecated static void removeTask(@NotNull Task t, @Nullable String reason, @NotNull List<Task> displ) {
        t.log(reason);
        displ.add(t);
    }

    int capacity();


    /**
     * number of items in this collection
     */
    int size();

    boolean isEmpty();





//    @Nullable
//    default BivariateGridInterpolator getWaveFrequencyConfidenceTime() {
//        return null;
//    }
//
//    @Nullable
//    default UnivariateInterpolator getWaveFrequencyConfidence() {
//        return null;
//    }
//
//    @Nullable
//    default UnivariateInterpolator getWaveConfidenceTime() {
//        return null;
//    }

    default void top(int _maxPerConcept, @NotNull Consumer<Task> recip) {
        int s = size();
        final int[] maxPerConcept = {Math.min(s, _maxPerConcept)};
        forEach(t -> {
            if ((maxPerConcept[0]--) >= 0)
                recip.accept(t);
            //if (--maxPerConcept == 0) break; //TODO use a forEachWhile w/ Predicate or something
        });
    }


    //boolean contains(Task t);

//    @Nullable
//    QuestionTable EMPTY = new QuestionTable() {
//
//        @Override
//        public
//        @Nullable
//        Task add(Task t, Memory m) {
//            return t;
//        }
//
//        @Override
//        public
//        @Nullable
//        Task get(Task t) {
//            return null;
//        }
//
//        @Override
//        public Iterator<Task> iterator() {
//            return Iterators.emptyIterator();
//        }
//
//        @Override
//        public int capacity() {
//            return 0;
//        }
//
//
//        @Override
//        public void setCapacity(int newCapacity) {
//
//        }
//
//        @Override
//        public int size() {
//            return 0;
//        }
//
//        @Override
//        public void clear() {
//
//        }
//
//        @Override
//        public boolean isEmpty() {
//            return true;
//        }
//
//        @Override
//        public void remove(@NotNull Task belief, @NotNull NAR nar) {
//            throw new UnsupportedOperationException();
//        }
//
//
//
//    };

//    /** forcibly remove a held Task
//     *  should eventually invoke TaskTable.removeTask() */
//    void remove(@NotNull Task belief, List<Task> displ);


    //void add(Task incoming, List<Task> displaced);

    //Task put(Task incoming);
}