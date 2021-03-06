package jcog.meter.event;

import jcog.math.RecycledSummaryStatistics;
import org.eclipse.collections.api.block.procedure.primitive.FloatProcedure;

/** buffers the result to avoid returning an incomplete value
 *  NOT thread safe
 * */
public class BufferedFloatGuage implements FloatProcedure {

    final RecycledSummaryStatistics data = new RecycledSummaryStatistics();
    float mean, sum;

    public final String id;

    public BufferedFloatGuage(String id) {
        super();
        this.id = id;
    }

    public float getSum() {
        return sum;
    }
    public float getMean() {
        return mean;
    }

    /** records current values and clears for a new cycle */
    public void clear() {
        mean = (float) data.getMean();
        sum = (float) data.getSum();
        data.clear();
    }

    public void accept(float v) {
        data.accept(v);
    }

    @Override
    public final void value(float v) {
        accept(v);
    }
}
