package jcog.math;

import jcog.data.map.MRUCache;
import org.eclipse.collections.api.block.function.primitive.FloatFunction;

import java.util.Iterator;
import java.util.Map;

public class BoundedFloatFunctionCache<X> extends MRUCache<X, Float> implements FloatFunction<X> {

    private final FloatFunction<X> f;

    public BoundedFloatFunctionCache(FloatFunction<X> f, int capacity) {
        super(capacity);
        this.f = f;
    }

//    @Override
//    public final float floatValueOf(X x) {
//        return computeIfAbsent(x, f::floatValueOf);
//    }


    @Override
    public float floatValueOf(X x) {

        synchronized (f) {
            Float f = get(x);
            if (f != null)
                return f;
        }

        float v = f.floatValueOf(x);

        synchronized (f) {
            put(x, v);
        }

        return v;
    }

    public void forget(float pct) {
        int toForget = (int) Math.ceil(size()*pct);
        if (toForget > 0) {
            synchronized (f) {

                Iterator<Map.Entry<X,Float>> ii = entrySet().iterator();
                while (--toForget > 0 && ii.hasNext()) {
                    onEvict(ii.next());
                    ii.remove();
                }

            }
        }
    }
}
