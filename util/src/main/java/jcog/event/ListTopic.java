package jcog.event;

import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**  arraylist implementation, thread safe.  creates an array copy on each update
 *   for fastest possible iteration during emitted events. */
public class ListTopic<V> extends jcog.list.FastCoWList<Consumer<V>> implements Topic<V> {

    public ListTopic() {
        super(8, Consumer[]::new);
    }

    @Override
    public final void emit(V x) {
        final Consumer[] cc = this.copy;
        if (cc!=null) {
            for (Consumer c : cc)
                c.accept(x);
        }
    }

    @Override
    public void emitAsync(V x, Executor executorService) {
        final Consumer[] cc = this.copy;
        if (cc!=null) {
            for (Consumer c : cc)
                executorService.execute(() -> c.accept(x));
        }
    }

// TODO
//    public void emitAsync(V x, Consumer<Iterable<Consumer<V>>> executorService) {
//        final Consumer[] cc = this.copy;
//        if (cc!=null) {
//            for (Consumer c : cc)
//                executorService.execute(() -> c.accept(x));
//        }
//    }

    @Override public final void enable(Consumer<V> o) {
        add(o);
    }

    @Override public final void disable(Consumer<V> o) {
        remove(o);
    }


}