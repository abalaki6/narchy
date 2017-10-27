package nars.bag;

import jcog.Util;
import jcog.bag.impl.ArrayBag;
import jcog.pri.Priority;
import jcog.pri.op.PriMerge;
import jcog.util.QueueLock;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

abstract public class ConcurrentArrayBag<K,X extends Priority> extends ArrayBag<K,X> {

    private final QueueLock<X> toPut;


    protected ConcurrentArrayBag(PriMerge mergeFunction, int cap) {
        this(mergeFunction, new HashMap<>(cap), cap);
    }

    protected ConcurrentArrayBag(PriMerge mergeFunction, @NotNull Map<K, X> map, int cap) {
        super(mergeFunction, map);
        setCapacity(cap);

        this.toPut = new QueueLock<X>(Util.blockingQueue(cap), super::putAsync, (batchSize) -> {
            if (mustSort) {
                synchronized (items) {
                    super.ensureSorted();
                }
            }
        });
    }

    @Override
    protected void ensureSorted() {
        //sort elides until after batchFinished
    }

    @Override
    public void putAsync(X b) {
        toPut.accept(b);
    }



}

