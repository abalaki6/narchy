package nars.util.exe;

import com.google.common.base.Joiner;
import jcog.bag.Bag;
import jcog.bag.impl.ArrayBag;
import jcog.bag.impl.HijackBag;
import jcog.bag.impl.hijack.PriorityHijackBag;
import jcog.data.FloatParam;
import jcog.math.MultiStatistics;
import jcog.math.RecycledSummaryStatistics;
import jcog.pri.Pri;
import jcog.pri.mix.control.CLink;
import jcog.pri.op.PriMerge;
import nars.NAR;
import nars.Task;
import nars.task.ITask;
import nars.task.NALTask;
import nars.truth.Truthed;
import org.eclipse.collections.impl.map.mutable.primitive.ObjectFloatHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static nars.Op.BELIEF;
import static nars.Op.COMMAND;

/**
 * Buffers all executions between each cycle in order to remove duplicates
 */
public class TaskExecutor extends Executioner {

    //    private final DisruptorBlockingQueue<CLink<ITask>> overflow;
    protected boolean trace;

    /**
     * if < 0, executes them all. 0 pauses, and finite value > 0 will cause them to be sorted first if the value exceeds the limit
     * interpreted as its integer value, although currently it is FloatParam
     */
    public final FloatParam exePerCycleMax = new FloatParam(-1);

//    /**
//     * temporary collection of tasks to remove after sampling
//     */
//    protected final FasterList<ITask> toRemove = new FasterList();

    //    /**
//     * amount of priority to subtract from each processed task (re-calculated each cycle according to bag pressure)
//     */
//    protected float forgetEachPri;
    public final FloatParam masterGain = new FloatParam(1f, 0f, 1f);



    public final Bag<ITask, CLink<ITask>> nal = new ArrayBag<>(0, PriMerge.max, new ConcurrentHashMap<>()) {

        @Override
        public float floatValueOf(CLink<ITask> x) {
            return x.pri();
        }

        @Nullable
        @Override
        public ITask key(@NotNull CLink<ITask> l) {
            return l.ref;
        }
    };


    /**
     * active tasks
     */
    public final Bag<ITask, CLink<ITask>> active =
//            new ArrayBag<>(PriMerge.plus, new ConcurrentHashMap<>()) {


            new PriorityHijackBag<>(4) {
                @Override
                protected final Consumer<CLink<ITask>> forget(float rate) {
                    return null;
                }

                @Override
                public Bag<ITask, CLink<ITask>> commit() {
                    return this; //do nothing
                }

                @Override
                public HijackBag commit(Consumer c) {
                    return this; //do nothing
                }

//                @Override
//                public CLink<ITask> put(@NotNull CLink<ITask> x) {
//                    CLink<ITask> y = super.put(x);
////                    if (y == null) {
////                        overflow.offer(x);
////                    }
//                    return y;
//                }

//                @Override
//                public void onRemoved(@NotNull CLink<ITask> value) {
//
//                    //DO NOTHING, DONT DELETE
//
////                    if (value.priElseZero() >= Pri.EPSILON) {
////                        if (overflow.remainingCapacity() < 1) {
////                            overflow.poll(); //forget
////                        }
////                        overflow.offer(value); //save
////                    } else {
////                        CLink<ITask> x = overflow.poll();
////                        if (x != null && x.priElseZero() >= Pri.EPSILON)
////                            put(x); //restore
////                    }
//                }
//

                @NotNull
                @Override
                public final ITask key(CLink<ITask> value) {
                    return value.ref;
                }


            };

    //final DecideRoulette<CLink<ITask>> activeBuffer = new DecideRoulette<>(CLink::priElseZero);

    private float forgetEachActivePri;


    public TaskExecutor(int capacity, int taskCapacity) {
        super();
        active.setCapacity(capacity);
        nal.setCapacity(taskCapacity);

        //int overCapacity = capacity / 2;
        //overflow = new DisruptorBlockingQueue(overCapacity);
    }

    public TaskExecutor(int capacity, float executedPerCycle) {
        this(capacity, capacity * 2);
        exePerCycleMax.setValue(Math.ceil(capacity * executedPerCycle));
    }

    @Override
    public void cycle(@NotNull NAR nar) {
        //flush();

        nar.eventCycleStart.emit(nar);

        flush();
    }

    @Override
    public int concurrency() {
        return 1;
    }

    @Override
    public void stop() {
        flush();
        super.stop();
    }


    @Override
    public void start(NAR nar) {
        super.start(nar);
        flush(); //<- may not be necessary
    }

//    @Override
//    public void stop() {
//        flush();
//        super.stop();
//    }

    AtomicBoolean busy = new AtomicBoolean(false);


    @Override
    public void forEach(Consumer<ITask> each) {
        nal.forEachKey(each);
        active.forEachKey(each);
    }


    @Override
    public void runLater(Runnable r) {
        r.run(); //synchronous
    }


    protected void flush() {
        if (!busy.compareAndSet(false, true))
            return;

        try {

            //active.commit(null);

            int ps = (nal.size() + active.size());
            if (ps == 0)
                return;

            boolean t = this.trace;
            if (t)
                active.print();

            int toExe = exePerCycleMax.intValue();
            if (toExe < 0)
                toExe = active.capacity();


            toExe = Math.min(ps, toExe);
            int toInput = nal.size();

                //Random rng = nar.random();

            //INPUT
            nal.commit(null).pop(toInput, this::actuallyRun);

            //EXEC
            float eFrac = ((float) toExe) / active.capacity();
            float pAvg = (1f /*PForget.DEFAULT_TEMP*/) * ((HijackBag) active).depressurize(eFrac) * (eFrac);
            this.forgetEachActivePri =
                    pAvg > Pri.EPSILON * 8 ? pAvg : 0;
                    //0;

            //activeBuffer.clear();
            active.sample(Math.min(active.size(), Math.max(1, toExe*2)), x -> {

                actuallyRun(x);

                if (forgetEachActivePri > 0) {
                    x.priSub(forgetEachActivePri);
                }

                //activeBuffer.add(x);
                //(Consumer<? super CLink<ITask>>)(buffer::add)
            });

//            for (int i = 0; i < toExe; i++) {
//                @Nullable CLink<ITask> x = activeBuffer.decide(rng);
//                actuallyRun(x);
//                if (forgetEachActivePri > 0) {
//                    x.priSub(forgetEachActivePri);
//                }
//            }

            //active.sample(toExe, this::actuallyRun);

//            if (!toRemove.isEmpty()) {
//                toRemove.clear(active::remove);
//            }

//            } else {
//                //sort
//                if (sorted == null || sorted.capacity() != (toExe + 1)) {
//                    sorted = new SortedArray<ITask>(new ITask[toExe + 1]);
//                }
//                pending.sample(pending.capacity(), s -> {
//                    sorted.add(s, Prioritized::oneMinusPri);
//                    if (sorted.size() > toExe)
//                        sorted.removeLast();
//                });
//                assert (sorted.size() == toExe);
//                sorted.forEach(this::actuallyRun);
//            }


        } finally {
            busy.set(false);
        }
    }

    protected void actuallyRun(CLink<? extends ITask> x) {
        ITask[] next;
        try {
            if (x == null) return; //HACK

            if (x.isDeleted()) {
                next = null;
            } else {
                next = x.ref.run(nar);
            }

        } catch (Throwable e) {
            NAR.logger.error("{} {}", x, e /*(Param.DEBUG) ? e : e.getMessage()*/);
            x.delete();
            return;
        }

        if (next == ITask.DeleteMe) {
            x.delete();
        } else {
            float g = masterGain.floatValue();
            if (g != 1)
                x.priMult(g);
        }

        actuallyFeedback(x, next);
    }

    protected void actuallyFeedback(CLink<? extends ITask> x, ITask[] next) {
        if (next != null && next.length > 0)
            nar.input(next);
    }


    @Override
    public boolean run(@NotNull CLink<ITask> input) {
        if (input.ref.punc() == COMMAND) {
            actuallyRun(input); //commands executed immediately
            return true;
        } else {
            if (input.ref instanceof NALTask) {
                nal.putAsync(input);
            } else
                active.putAsync(input);

            return true;//!= null;
        }
    }


    public CharSequence stats() {

        RecycledSummaryStatistics pri = new RecycledSummaryStatistics();

        ObjectFloatHashMap<Class<? extends ITask>> typeToPri = new ObjectFloatHashMap();
        //.value("pri", x -> x.priElseZero());


        MultiStatistics<NALTask> beliefs = new MultiStatistics<NALTask>()
                .value("pri", Task::pri)
                .value("freq", Truthed::freq)
                .value("conf", Truthed::conf);

        active.forEachKey(x -> {
            float p = x.pri();
            if (p != p)
                return;
            typeToPri.addToValue(x.getClass(), p);
            if (x.punc() == BELIEF) {
                beliefs.accept((NALTask) x);
            }
            pri.accept(p);
        });

        //.classify("type", x -> x.getClass().toString()

        return Joiner.on("\n").join(typeToPri, beliefs, pri);
    }
}
