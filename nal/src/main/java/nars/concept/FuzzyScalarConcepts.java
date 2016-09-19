package nars.concept;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterators;
import nars.$;
import nars.NAR;
import nars.Symbols;
import nars.util.math.FloatSupplier;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import static nars.$.t;

/**
 * manages a set of concepts whose beliefs represent components of an
 * N-ary (N>=1) discretization of a varying scalar (32-bit floating point) signal.
 * expects values which have been normalized to 0..1.0 range (ex: use NormalizedFloat) */
public class FuzzyScalarConcepts implements Iterable<SensorConcept> {


    private final FloatSupplier input;
    @NotNull
    public final List<SensorConcept> sensors;
    @NotNull
    public final NAR nar;

    float conf;


    @NotNull final float[] centerPoints;

    public float value(int index) {
        float v = input.asFloat();

        int n = centerPoints.length;
        //float nearness[] = new float[n];
        float s = 0;
        float dr = 1f / (n-1);
        float numerator = Float.NaN;
        for (int i = 0; i < n; i++) {
            float dist = Math.abs(centerPoints[i] - v);
            float nn = Math.max(0, dr-dist);
            s += nn;
            if (i == index)
                numerator = nn;
        }
        return numerator /= s;
    }

    public FuzzyScalarConcepts(@NotNull MutableFloat input, @NotNull NAR nar, @NotNull String... states) {
        this(input::floatValue, nar, states);
    }

    public FuzzyScalarConcepts(FloatSupplier input, @NotNull NAR nar, @NotNull String... states) {


        this.conf = nar.confidenceDefault(Symbols.BELIEF);
        this.input = input;
        this.nar = nar;

        int numStates = states.length;
        centerPoints = new float[numStates];
        this.sensors = $.newArrayList(numStates);

        if (states.length > 1) {
            float dr = 1f / (numStates-1);
            float center = 0;
            int i = 0;
            for (String s : states) {

                centerPoints[i] = center;
                int ii = i;

                sensors.add( new SensorConcept(s, nar, this.input,
                        (x) -> t(value(ii), conf)
                ));
                center += dr;
                i++;
            }
        } else {
            sensors.add( new SensorConcept(states[0], nar, this.input,
                    (x) -> t(x, conf)
            ));
        }



    }

//		private Truth biangular(float v) {
//			if (v < 0.5f) return t(0, conf);
//			else {
//				//return t(1f, conf * Math.min(1f,(v-0.5f)*2f));
//				return t(v, conf);
//			}
//		}
//
//		private Truth triangular(float v) {
//			float f, c;
//			if (v < 0.66f && v > 0.33f) {
//				f = 0.5f;
//				c = (0.33f-Math.abs(v-0.5f)) * 3f;
//			} else {
//				f = (v > 0.5f) ? 1 : 0;
//				if (v > 0.5f) {
//					c = Math.abs(v - 0.66f) * 3f;
//				} else {
//					c = Math.abs(v - 0.33f) * 3f;
//				}
//			}
//			c = Util.clamp(c);
//
//			return t(f, c * conf);
//		}

//    @NotNull
//    public FuzzyScalar pri(float p) {
//        for (int i = 0, sensorsSize = sensors.size(); i < sensorsSize; i++) {
//            sensors.get(i).pri(p);
//        }
//        return this;
//    }


    @Override
    public void forEach(Consumer<? super SensorConcept> action) {
        sensors.forEach(action);
    }

    @NotNull
    public FuzzyScalarConcepts resolution(float r) {
        for (int i = 0, sensorsSize = sensors.size(); i < sensorsSize; i++) {
            sensors.get(i).resolution(r);
        }
        return this;
    }

    @NotNull
    public FuzzyScalarConcepts conf(float c) {
        this.conf = c;
        return this;
    }


    @NotNull
    @Override
    public String toString() {
        return Joiner.on("\t").join(Iterators.transform(
                sensors.iterator(), s -> {
                    return s.term() + " " + s.beliefs().truth(nar.time()).toString();
                }
        ));
    }

//    /** clear all sensor's belief state */
//    public void clear() {
//        sensors.forEach(s -> s.beliefs().clear());
//    }


    @NotNull
    @Override
    public Iterator<SensorConcept> iterator() {
        return sensors.iterator();
    }
}
///**
// * SensorConcept which wraps a MutableFloat value
// */
//public class FloatConcept extends SensorConcept {
//
//
//    @NotNull
//    private final MutableFloat value;
//
//    public FloatConcept(@NotNull String compoundTermString, @NotNull NAR n) throws Narsese.NarseseException {
//        this(compoundTermString, n, Float.NaN);
//    }
//
//    public FloatConcept(@NotNull String compoundTermString, @NotNull NAR n, float initialValue) throws Narsese.NarseseException {
//        this(compoundTermString, n, new MutableFloat(initialValue));
//    }
//
//    public FloatConcept(@NotNull String compoundTermString, @NotNull NAR n, @NotNull MutableFloat v) throws Narsese.NarseseException {
//        super(compoundTermString, n, v::floatValue,
//                (vv) -> new DefaultTruth(vv, n.confidenceDefault(Symbols.BELIEF) )
//        );
//        this.value = v;
//    }
//
//    public float set(float v) {
//        value.setValue(v);
//        return v;
//    }
//
//}