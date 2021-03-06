package spacegraph;

import jcog.data.map.MRUCache;

import java.util.Map;
import java.util.function.Function;

public class SpatialCache<X, Y extends Spatial<X>> {

    private final MRUCache<X, Y> atoms;
    private final SpaceGraph space;

    public SpatialCache(SpaceGraph space, int capacity) {
        this.space = space;
        atoms = new MRUCache<>(capacity) {
            @Override
            protected void onEvict(Map.Entry<X, Y> entry) {
                space.remove(entry.getValue());
            }
        };
//        Cache<X, Spatial<X>> atoms =
//                //new NonBlockingHashMap(cacheCapacity);
//                //new ConcurrentHashMap<>(cacheCapacity);
//                Caffeine.newBuilder()
//                        //.softValues().builder();
//                        .removalListener((X k, Spatial<X> v, RemovalCause c) -> {
//                            if (v!=null)
//                                v.delete(dyn);
//                        })
//                        //.maximumSize(cacheCapacity)
//                        .weakValues()
//                        .build();
//        this.atoms = atoms;


    }

    public Y getOrAdd(X x, Function<X, Y> materializer) {
        Y y = atoms.computeIfAbsent(x, materializer);
        y.activate();
        return y;
    }

    public Y get(Object x) {
        Spatial y = atoms.get(x);
        if (y != null)
            y.activate();
        return (Y) y;
    }


    public void remove(X x) {
        Y y = atoms.remove(x);
        if (y != null) {
            space.remove(y);
        }
    }
}
