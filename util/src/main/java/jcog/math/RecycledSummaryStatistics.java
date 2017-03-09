package jcog.math;

import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.eclipse.collections.api.block.procedure.primitive.FloatProcedure;

import java.io.Serializable;

/**
 * floatSummaryStatistics in java.util can't be cleared
 * wtf
 * anyway we'll add stdev calcluation to this and it will
 * serve as a lighter weight replacement for Apache
 * Commons Math SummaryStatistics which also is undesirable
 *
 */
public class RecycledSummaryStatistics implements FloatProcedure, Serializable, StatisticalSummary {
    protected long count;



    protected double sSum;
    //private float sumCompensation; // Low order bits of sum
//    private float simpleSum; // Used to compute right sum for non-finite inputs
    protected double min;
    protected double max;
    protected double mean;

    /**
     * Construct an empty instance with zero count, zero sum,
     * {@code float.POSITIVE_INFINITY} min, {@code float.NEGATIVE_INFINITY}
     * max and zero average.
     */
    public RecycledSummaryStatistics() {
        clear();
    }

    @Override
    public final void value(float each) {
        accept(each);
    }

    public final void clear() {
        count = 0;
        sSum = 0;
        mean = 0;
        min = Float.POSITIVE_INFINITY;
        max = Float.NEGATIVE_INFINITY;
    }
    /**
     * Records another value into the summary information.
     *
     * @param value the input value
     *
     * NOT THREAD SAFE
     */
    public final void accept(double value) {

        //http://stackoverflow.com/a/36590815
        //"waldorf method"
        double tmpMean = mean;
        if (tmpMean!=tmpMean)
            mean = tmpMean = 0;
        double delta = value - tmpMean;
        mean += delta / ++count;
        sSum += delta * (value - mean);

        //sumWithCompensation(value);
        if (min > value) min = value;
        if (max < value) max = value;
    }

//    /**
//     * Incorporate a new float value using Kahan summation /
//     * compensated summation.
//     */
//    private final void sumWithCompensation(float value) {
//        float tmp = value - sumCompensation;
//        float velvel = sum + tmp; // Little wolf of rounding error
//        sumCompensation = (velvel - sum) - tmp;
//        sum = velvel;
//    }

    /**
     * Returns the sum of values recorded, or zero if no values have been
     * recorded.
     *
     * If any recorded value is a NaN or the sum is at any point a NaN
     * then the sum will be NaN.
     *
     * <p> The value of a floating-point sum is a function both of the
     * input values as well as the order of addition operations. The
     * order of addition operations of this method is intentionally
     * not defined to allow for implementation flexibility to improve
     * the speed and accuracy of the computed result.
     *
     * In particular, this method may be implemented using compensated
     * summation or other technique to reduce the error bound in the
     * numerical sum compared to a simple summation of {@code float}
     * values.
     *
     * @apiNote Values sorted by increasing absolute magnitude tend to yield
     * more accurate results.
     *
     * @return the sum of values, or zero if none
     */
    @Override
    public final double getSum() {
        return (float) getMean() * count;

//        // Better error bounds to add both terms as the final sum
//        float tmp =  sum + sumCompensation;
//        if (float.isNaN(tmp) && float.isInfinite(simpleSum))
//            // If the compensated sum is spuriously NaN from
//            // accumulating one or more same-signed infinite values,
//            // return the correctly-signed infinity stored in
//            // simpleSum.
//            return simpleSum;
//        else
//            return tmp;
    }

    /**
     * Returns the minimum recorded value, {@code float.NaN} if any recorded
     * value was NaN or {@code float.POSITIVE_INFINITY} if no values were
     * recorded. Unlike the numerical comparison operators, this method
     * considers negative zero to be strictly smaller than positive zero.
     *
     * @return the minimum recorded value, {@code float.NaN} if any recorded
     * value was NaN or {@code float.POSITIVE_INFINITY} if no values were
     * recorded
     */
    @Override
    public final double getMin() {
        return min;
    }

    @Override
    public long getN() {
        return count;
    }

    /**
     * Returns the maximum recorded value, {@code float.NaN} if any recorded
     * value was NaN or {@code float.NEGATIVE_INFINITY} if no values were
     * recorded. Unlike the numerical comparison operators, this method
     * considers negative zero to be strictly smaller than positive zero.
     *
     * @return the maximum recorded value, {@code float.NaN} if any recorded
     * value was NaN or {@code float.NEGATIVE_INFINITY} if no values were
     * recorded
     */
    @Override
    public final double getMax() {
        return max;
    }

    /**
     * {@inheritDoc}
     *
     * Returns a non-empty string representation of this object suitable for
     * debugging. The exact presentation format is unspecified and may vary
     * between implementations and versions.
     */
    @Override
    public String toString() {
        return String.format(
                "%s{n=%d, sum=%f, min=%f, avg=%f, max=%f}",
                getClass().getSimpleName(),
                getN(),
                getSum(),
                getMin(),
                (float) getMean(),
                getMax());
    }

    public final double normalize(float n) {
        double min = getMin();
        double max = getMax();
        double range = max - min;
        if (range < Float.MIN_VALUE*64f /* estimate of an FP epsilon */)
            return 0.5f;
        else
            return (n - min) / (range);
    }

    /**
     * Returns the standard deviation of the values that have been added.
     * <p>
     * Double.NaN is returned if no values have been added.
     * </p>
     * The Standard Deviation is a measure of how spread out numbers are.
     * @return the standard deviation
     */
    @Override
    public double getStandardDeviation() {
        double v = getVariance();
        if (v==v)
            return (float) Math.sqrt(v);
        else
            return Float.NaN;
    }

    @Override
    public double getMean() {
        return mean;
    }

    @Override
    public double getVariance() {
        long c = count;
        if (c == 0) return Float.NaN;
        return sSum / (c);
    }

}
