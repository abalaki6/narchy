package nars.util.learn;

import com.gs.collections.api.block.function.primitive.FloatFunction;
import com.gs.collections.api.block.function.primitive.FloatToFloatFunction;
import nars.NAR;
import nars.Symbols;
import nars.task.MutableTask;
import nars.task.Task;
import nars.term.Term;
import nars.term.Termed;
import nars.util.data.Util;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.DoubleSupplier;

/**
 * Created by me on 2/2/16.
 */
public class Sensor implements Consumer<NAR>, DoubleSupplier {

    /**
     * resolution of the output freq value
     */
    float resolution = 0.1f;

    private final Term term;
    private final FloatFunction<Term> value;
    private final FloatToFloatFunction freq;
    @NotNull
    private final NAR nar;
    private float pri;
    private final float dur;
    private float conf;
    private float prevF;

    boolean inputIfSame = false;
    int maxTimeBetweenUpdates;
    int minTimeBetweenUpdates;


    private long lastInput;

    final static FloatToFloatFunction direct = n -> n;

    public Sensor(@NotNull NAR n, Termed  t, FloatFunction<Term> value) {
        this(n, t, value, direct);
    }

    public Sensor(@NotNull NAR n, String tt, FloatFunction<Term> value) {
        this(n, tt, value, direct);
    }

    public Sensor(@NotNull NAR n, String tt, FloatFunction<Term> value, FloatToFloatFunction valueToFreq) {
        this(n, n.term(tt), value, valueToFreq);
    }

    public Sensor(@NotNull NAR n, Termed  t, FloatFunction<Term> value, FloatToFloatFunction valueToFreq) {
        this(n, t, value, valueToFreq, n.getDefaultConfidence(Symbols.BELIEF),
                n.DEFAULT_JUDGMENT_PRIORITY, n.DEFAULT_JUDGMENT_DURABILITY);
    }

    public Sensor(@NotNull NAR n, Termed t, FloatFunction<Term> value, FloatToFloatFunction valueToFreq, float conf, float pri, float dur) {
        this.nar = n;
        this.term = t.term();
        n.onFrame(this);
        this.value = value;
        this.freq = valueToFreq;
        this.conf = conf;

        this.pri = pri;
        this.dur = dur;
        this.lastInput = n.time() - 1;

        this.prevF = Float.NaN;
    }

    public Sensor pri(float defaultPri) {
        this.pri = defaultPri;
        return this;
    }

    @Override
    public void accept(@NotNull NAR nar) {

        int timeSinceLastInput = (int)(nar.time() - lastInput);


        double nextD = value.floatValueOf(term);
        if (!Double.isFinite(nextD))
            return; //allow the value function to prevent input by returning NaN
        float next = (float) nextD;

        float fRaw = freq.valueOf(next);
        if (!Float.isFinite(fRaw))
            return; //allow the frequency function to prevent input by returning NaN



        float f = Util.round(fRaw, resolution);
        if (inputIfSame || (f != prevF) || (maxTimeBetweenUpdates !=0 && timeSinceLastInput>= maxTimeBetweenUpdates)) {
            if (minTimeBetweenUpdates!=0 && timeSinceLastInput >= minTimeBetweenUpdates) {
                Task t = input(f);
                this.lastInput = t.creation();
            }
        }

        this.prevF = f;

        //this.prevValue = next;
    }

    public Sensor resolution(float r) {
        this.resolution = r;
        return this;
    }

    @NotNull
    private Task input(float v) {
        float f, c;
        if (v < 0.5f) {
            f = 0f;
            c = (0.5f - v)*(2f * conf);
        } else {
            f = 1f;
            c = (v - 0.5f)*(2f * conf);
        }


        Task t = new MutableTask(term).belief()
                //.truth(v, conf)
                .truth(f, c)
                .present(nar.time()).budget(pri, dur);
        nar.input(t);
        return t;
    }

    @Override
    public double getAsDouble() {
        return prevF;
    }

    
    /** sets default confidence */
    @NotNull
    public Sensor conf(float conf) {
        this.conf = conf;
        return this;
    }

    /** sets minimum time between updates, even if nothing changed. zero to disable this */
    @NotNull
    public Sensor maxTimeBetweenUpdates(int dt) {
        this.maxTimeBetweenUpdates = dt;
        return this;
    }
    @NotNull
    public Sensor minTimeBetweenUpdates(int dt) {
        this.minTimeBetweenUpdates = dt;
        return this;
    }

    //        public void on() {
//
//        }
//        public void off() {
//
//        }
}
