package nars.nar;

import jcog.AffinityExecutor;
import jcog.Util;
import jcog.bag.Bag;
import jcog.bag.impl.hijack.DefaultHijackBag;
import jcog.event.On;
import jcog.pri.PLink;
import jcog.pri.PriMerge;
import nars.$;
import nars.NAR;
import nars.NARLoop;
import nars.Task;
import nars.conceptualize.DefaultConceptBuilder;
import nars.index.term.TermIndex;
import nars.index.term.map.CaffeineIndex;
import nars.task.ITask;
import nars.term.Term;
import nars.time.Time;
import nars.util.exe.BufferedSynchronousExecutor;
import nars.util.exe.Executioner;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * recursive cluster of NAR's
 * <sseehh> any hierarchy can be defined including nars within nars within nars
 * <sseehh> each nar runs in its own thread
 * <sseehh> they share concepts
 * <sseehh> but not the importance of concepts
 * <sseehh> each one has its own concept attention
 * <sseehh> link attention is currently shared but ill consider if this needs changing
 */
public class NARS extends NAR {

    private static final float SYNC_HZ_DEFAULT = 30f;

    public final List<NAR> sub = $.newArrayList();
    private final List<On> observers = $.newArrayList();
    int num;

    private AffinityExecutor pool;
    private List<NARLoop> loops;

    @Override
    public void input(ITask... t) {

        //assert (!nar.isEmpty());
        for (ITask x : t)
            dispatch(x);
    }

    public void dispatch(ITask x) {
        int remain = num;
        int start = random().nextInt(remain);
        while (remain-- > 0) {
            NAR target = this.sub.get(start); //random distribution TODO abstract to other striping policies
            if (target.exe.run(x))
                break; //accepted
        }
    }

    @FunctionalInterface
    public interface NARSSupplier {
        NAR build(Time time, TermIndex terms, Random rng);
    }

    public void addNAR(NARSSupplier n) {
        synchronized (terms) {
            assert (!running());
            NAR x = n.build(time, terms, random());
            sub.add(x);
            num = sub.size();
            observers.add(x.eventTaskProcess.on(eventTaskProcess::emit)); //proxy
        }
    }

    /**
     * default implementation convenience method
     */
    public void addNAR(int capacity) {
        addNAR((time, terms, rng) -> {
            SubExecutor e = new SubExecutor(capacity, 0.5f);
            Default d = new Default(rng, terms, time, e);
            return d;
        });
    }

    class SubExecutor extends BufferedSynchronousExecutor {
        public SubExecutor(int inputQueueCapacity, float exePct) {
            super(inputQueueCapacity, exePct);
        }


        //    class SubExecutor extends BufferedSynchronousExecutor {
//        public SubExecutor(int inputQueueCapacity) {
//            super(
//                new DisruptorBlockingQueue<ITask>(inputQueueCapacity)
//            );
//        }

        @Override
        public void runLater(@NotNull Runnable r) {
            pool.execute(r); //use the common threadpool
        }
    }

    public static class ExperimentalConceptBuilder extends DefaultConceptBuilder {
        public static final int reprobes = 3;
        //                (
//                new DefaultConceptState("sleep", 16, 16, 3, 24, 16),
//                new DefaultConceptState("awake", 32, 32, 3, 24, 16)
//        );

        @Override
        public <X> X withBags(Term t, BiFunction<Bag<Term, PLink<Term>>, Bag<Task, PLink<Task>>, X> f) {

            Bag<Term, PLink<Term>> termlink =
                    new DefaultHijackBag<>(DefaultConceptBuilder.DEFAULT_BLEND, reprobes);
            //BloomBag<Term> termlink = new BloomBag<Term>(32, IO::termToBytes);

            Bag<Task, PLink<Task>> tasklink = new DefaultHijackBag<>(DefaultConceptBuilder.DEFAULT_BLEND, reprobes);

            return f.apply(termlink, tasklink);
        }

        @NotNull
        @Deprecated
        @Override
        public <X> Bag<X, PLink<X>> newBag(@NotNull Map m, PriMerge blend) {
            return new DefaultHijackBag<>(blend, reprobes);
        }
    }

    NARS(@NotNull Time time, @NotNull Random rng, Executioner e) {
        super(time,
                //new HijackTermIndex(new DefaultConceptBuilder(), 256 * 1024, 3),
                new CaffeineIndex(new DefaultConceptBuilder(), 256 * 1024, e),
                rng, e);
    }


    public NARS(@NotNull Time time, @NotNull Random rng, int passiveThreads) {
        this(time, rng,
                new Executioner() {

                    final ForkJoinPool passive =
                            new ForkJoinPool(passiveThreads);

                    @Override
                    public void run(@NotNull Consumer<NAR> r) {

                        //run in sub thread
//                        List<NAR> subs = ((NARS) nar).sub;
//                        int which = nar.random().nextInt(subs.size());
//                        subs.get(which).exe.run(r);

                        runLater(() -> r.accept(nar)); //passive
                    }

                    @Override
                    public void runLater(Runnable cmd) {
                        passive.execute(cmd);
                    }

                    @Override
                    public boolean run(@NotNull ITask t) {
                        throw new UnsupportedOperationException("should be intercepted by class NARS");
                    }

                    @Override
                    public void cycle(@NotNull NAR nar) {

                        int waitCycles = 0;

                        while (!passive.isQuiescent()) {
                            Util.pauseNext(waitCycles++);
                        }

                        Consumer[] vv = nar.eventCycleStart.getCachedNullTerminatedArray();
                        if (vv != null) {
                            for (int i = 0; ; ) {
                                Consumer c = vv[i++];
                                if (c == null) break; //null terminator hit

                                run(c);
                            }
                        }

                    }

                    @Override
                    public int concurrency() {
                        return 0; //TODO
                    }

                    @Override
                    public boolean concurrent() {
                        return true;
                    }

                    @Override
                    public void forEach(Consumer<ITask> each) {
                        throw new UnsupportedOperationException();
                    }
                });
    }




    public boolean running() {
        return this.loop.isRunning();
    }


    @Override
    public @NotNull NARLoop start() {
        return startFPS(SYNC_HZ_DEFAULT);
    }

    @Override
    public NARLoop startPeriodMS(int ms) {
        synchronized (terms) {

            assert (!running());

            int num = sub.size();

            this.loops = $.newArrayList(num);
            sub.forEach(n -> {
                NARLoop l = new NARLoop(n);
                l.prePeriodMS(0); //preset for infinite loop
                loops.add(l);
            });

            //this.pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(num);
            //this.pool.prestartAllCoreThreads();
            this.pool = new AffinityExecutor(self().toString());
            loops.forEach(pool::execute);
        }

        return super.startPeriodMS(ms);
    }

    @Override
    public void stop() {
        synchronized (terms) {
            if (!running())
                return;

            super.stop();

            loops.forEach(NARLoop::stop);
            this.loops = null;

            this.pool.shutdownNow();
            this.pool = null;

        }
    }

    public TreeMap<String, Object> stats() {
        synchronized (terms) {
            TreeMap<String, Object> m = new TreeMap();

            m.put("now", new Date());

            for (NAR n : sub) {
                m.put(n.self() + "_emotion", n.emotion.summary());
            }
            if (loops != null) {
                for (NARLoop l : loops) {
                    NAR n = l.nar;
                    m.put(n.self() + "_cycles", l.cycleCount());
                }
            }
            m.put("terms", terms.summary());
            return m;
        }
    }

//    public static void main(String[] args) {
//
//        NARS n = new NARS(
//                new RealTime.DSHalf(true),
//                new XorShift128PlusRandom(1), 2);
//
//
//        n.addNAR(2048);
//        n.addNAR(2048);
//
//        //n.log();
//
//        new DeductiveMeshTest(n, 5, 5);
//
//        n.start();
//
//        for (int i = 0; i < 10; i++) {
//            System.out.println(n.stats());
//            Util.sleep(500);
//        }
//
//        n.stop();
//    }
//

}