package nars.bag.leak;

import jcog.bag.Bag;
import jcog.bag.impl.ConcurrentArrayBag;
import jcog.math.FloatParam;
import jcog.pri.PLink;
import jcog.pri.op.PriMerge;
import nars.NAR;
import nars.Task;
import nars.exe.Causable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * interface for controlled draining of a bag
 * "leaky bucket" model
 */
public abstract class TaskLeak extends Causable {

    protected final DtLeak<Task, PLink<Task>> in;

    protected TaskLeak(int capacity, float ratePerDuration, @NotNull NAR n) {
        this(
                new ConcurrentArrayBag<Task,PLink<Task>>(PriMerge.max, capacity) {
                    @Nullable
                    @Override
                    public Task key(PLink<Task> t) {
                        return t.get();
                    }
                }, ratePerDuration, n
        );
    }

    protected TaskLeak(@NotNull Bag<Task, PLink<Task>> bag, float ratePerDuration, @NotNull NAR n) {
        this(bag, new FloatParam(ratePerDuration), n);
    }


    TaskLeak(@NotNull Bag<Task, PLink<Task>> bag, @NotNull FloatParam rate, @NotNull NAR n) {
        super(n);
        this.in = new DtLeak<>(bag, rate) {
            @Override
            protected float receive(PLink<Task> b) {
                Task t = b.get();
                if (t.isDeleted())
                    return 0f;
                else
                    return TaskLeak.this.leak(t);
            }
        };
    }

    @Override
    protected void start(NAR nar) {
        super.start(nar);
        ons.add(nar.onTask((t) -> accept(nar, t)));
    }


    @Override
    public void clear() {
        in.clear();
    }

    @Override
    protected int next(NAR nar, int work) {
//        return in.commit(nar.time(), dt, nar.dur(),
//                work/((float)in.bag.capacity()));

        if (in.isEmpty())
            return -1; //done for the cycle

        float done = in.commit(nar.time(),  nar.dur(), work);
        return Math.round(done);
    }

    public final void accept(NAR nar, Task t) {
        if (preFilter(t))
            in.put(new PLink<>(t, t.priElseZero()));
    }

    protected boolean preFilter(Task next) {
        return true;
    }

    /** returns how much of the input was consumed; 0 means nothing, 1 means 100% */
    abstract protected float leak(Task next);
}
