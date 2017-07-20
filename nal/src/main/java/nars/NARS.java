package nars;

import jcog.random.XorShift128PlusRandom;
import nars.conceptualize.ConceptBuilder;
import nars.conceptualize.DefaultConceptBuilder;
import nars.index.term.BasicTermIndex;
import nars.index.term.TermIndex;
import nars.index.term.map.CaffeineIndex;
import nars.nar.exe.BufferedExecutioner;
import nars.nar.exe.Executioner;
import nars.op.stm.STMTemporalLinkage;
import nars.time.CycleTime;
import nars.time.RealTime;
import nars.time.Time;
import org.jetbrains.annotations.NotNull;

import java.util.Random;
import java.util.function.Supplier;

/**
 * NAR builder
 */
public class NARS {

    public NAR get() {
        NAR n = new NAR(index.get(), exe.get(), time, rng.get(), concepts.get());
        init(n);
        return n;
    }

    /**
     * subclasses may override this to configure newly constructed NAR's
     */
    protected void init(NAR n) {

    }

    protected Supplier<TermIndex> index;

    protected Time time;

    protected Supplier<Executioner> exe;

    protected Supplier<Random> rng;

    protected Supplier<ConceptBuilder> concepts;


    public NARS index(@NotNull TermIndex concepts) {
        this.index = () -> concepts;
        return this;
    }

    public NARS time(@NotNull Time time) {
        this.time = time;
        return this;
    }

    public NARS exe(Executioner exe) {
        this.exe = () -> exe;
        return this;
    }

    public NARS concepts(ConceptBuilder cb) {
        this.concepts = () -> cb;
        return this;
    }

    /**
     * defaults
     */
    public NARS() {

        index = () ->
                //new CaffeineIndex(new DefaultConceptBuilder(), 8*1024, 16*1024, null)
                new BasicTermIndex(1 * 1024);

        time = new CycleTime();

        exe = () -> new BufferedExecutioner(64, 32, 0.2f);

        rng = () -> new XorShift128PlusRandom(1);

        concepts = () -> new DefaultConceptBuilder();

    }

    /**
     * safe for single-thread access only. full NAL8 with STM Linkage
     */
    public static NAR single() {
        return single(8);
    }


    /**
     * safe for single-thread access only. useful for unit tests or embedded components
     */
    public static NAR single(int nal) {
        return new Default(nal, false).get();
    }

    /** single-thread, limited to NAL6. suitable for embedded situations */
    public static NAR singleEternal() {
        return new Default(6, false).get();
    }

    /**
     * single thread but for multithread usage:
     *      unbounded soft reference index
     */
    public static NAR threadSafe() {
        return new Default(8, true).get();
    }

    /** default: thread-safe, with centisecond (0.01) precision realtime clock */
    public static NAR realtime() {
        return new Default(8, true).time(new RealTime.CS()).get();
    }

//    /** TODO: provides only low level functionality */
//    public static NAR terminal() {
//        return null;
//    }

    /** generic defaults */
    public static class Default extends NARS {

        final int nal;

        public Default(int nal, boolean threadSafe) {
            this.nal = nal;
            if (threadSafe)
                index = ()->new CaffeineIndex(-1);
        }

        @Override
        protected void init(NAR nar) {

            nar.nal(nal);

            nar.termVolumeMax.setValue(32);
            nar.confMin.setValue(0.05f);

            nar.DEFAULT_BELIEF_PRIORITY = 0.5f;
            nar.DEFAULT_GOAL_PRIORITY = 0.7f;
            nar.DEFAULT_QUESTION_PRIORITY = 0.25f;
            nar.DEFAULT_QUEST_PRIORITY = 0.35f;

            if (nal >= 7)
                new STMTemporalLinkage(nar, 2, false);

        }
    }

}
