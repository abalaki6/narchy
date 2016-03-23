package nars.truth;

import nars.Global;
import nars.Memory;
import nars.util.data.Util;
import org.jetbrains.annotations.NotNull;

import static nars.util.data.Util.round;


public class DefaultTruth implements Truth  {

    public final float freq, conf;
    public final int hash;

    /** unspecified confidence, will be invalid unless updated later */
    @Deprecated public DefaultTruth(float f) {
        this(f, Float.NaN);
    }

    @Override
    public final float freq() {
        return freq;
    }

    @Override
    public final float conf() {
        return conf;
    }

    public DefaultTruth(float f, float c) {
        this(f, c, Global.TRUTH_EPSILON, hashDiscreteness);
    }

    public DefaultTruth(float f, float c, float epsilon) {
        this(f, c, Global.TRUTH_EPSILON, (int)(1f/epsilon));
    }

    public DefaultTruth(float f, float c, float epsilon, int discreteness) {
        this.freq = round(f, epsilon);
        if (Float.isFinite(c)) {
            this.conf = round(c, epsilon);
            this.hash = Truth.hash(this, discreteness);
        } else {
            this.conf = c;
            this.hash = 0;
        }
    }

    public DefaultTruth(char punctuation, @NotNull Memory m) {
        this(1.0f, m.getDefaultConfidence(punctuation));
    }


    public DefaultTruth(@NotNull Truth truth) {
        this(truth.freq(), truth.conf());
    }

    @NotNull
    @Override
    public Truth confMult(float factor) {
        return (factor == 1) ? this : withConf(conf() * factor);
    }

    @NotNull
    @Override public final Truth withConf(float newConf) {
        newConf = round(newConf, Global.TRUTH_EPSILON);
        return (conf != newConf) ? new DefaultTruth(freq, newConf) : this;
    }

    @NotNull
    @Override
    public String toString() {
        //return DELIMITER + frequency.toString() + SEPARATOR + confidence.toString() + DELIMITER;

        //1 + 6 + 1 + 6 + 1
        return toCharSequence().toString();
    }

    @Override
    public final boolean equals(Object that) {
        //if (that instanceof DefaultTruth) {
            return ((DefaultTruth)that).hash == hash; //shortcut, since perfect hash for this instance
        /*} else if (that instanceof Truth) {
            return equalsTruth((Truth)that);
        }
        return false;*/
    }

    @Override
    public final int hashCode() {
        return hash;
    }

    protected boolean equalsFrequency(@NotNull Truth t) {
        return (Util.equals(freq, t.freq(), Global.TRUTH_EPSILON));
    }

    private static final int hashDiscreteness = (int)(1.0f / Global.TRUTH_EPSILON);

    /*    public float getEpsilon() {
        return DEFAULT_TRUTH_EPSILON;
    }*/

//    /** truth with 0.01 resolution */
//    public static class DefaultTruth01 extends DefaultTruth {
//
//        public DefaultTruth01(float f, float c) {
//            super(f, c);
//        }
//    }
//
//
//
//    /** truth with 0.1 resolution */
//    public static class DefaultTruth1 extends AbstractDefaultTruth {
//
//        @Override
//        public float getEpsilon() {
//            return 0.1f;
//        }
//    }
//
//
//    /** truth with 0.001 resolution */
//    public static class DefaultTruth001 extends AbstractDefaultTruth {
//
//        @Override
//        public float getEpsilon() {
//            return 0.001f;
//        }
//    }
//
//
//    /** truth with 0.05 resolution */
//    public static class DefaultTruth05 extends AbstractDefaultTruth {
//
//        @Override
//        public float getEpsilon() {
//            return 0.05f;
//        }
//    }

}
