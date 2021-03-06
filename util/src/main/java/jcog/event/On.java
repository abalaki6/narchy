package jcog.event;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.util.function.Consumer;

/**
 * Represents the active state of a topic stream
 */
abstract public class On<V> {

    public final Topic<V> topic;

    On(Topic<V> t) {
        this.topic = t;
    }

    abstract public void off();

    public static class Strong<V> extends On<V> {

        public final Consumer<V> reaction;

        Strong(Topic<V> t, Consumer<V> o) {
            super(t);
            reaction = o;
            t.enable(o);
        }

        @Override
        public void off() {
            topic.disable(reaction);
        }

        @Override
        public String toString() {
            return "On:" + topic + "->" + reaction;
        }
    }

    public static class Weak<V> extends On<V> implements Consumer<V> {


        protected static final Logger logger = LoggerFactory.getLogger(Weak.class);

        public final WeakReference<Consumer<V>> reaction;


        Weak(Topic<V> t, Consumer<V> o) {
            super(t);
            reaction = new WeakReference<Consumer<V>>(o);
            t.enable(this);
        }

        @Override
        public void accept(V v) {
            Consumer<V> c = reaction.get();
            if (c != null) {
                try {
                    c.accept(v);
                } catch (Throwable any) {
                    logger.error(" {}", any);
                    off();
                }
            } else {
                //reference has been lost, so unregister:
                off();
            }
        }

        @Override
        public void off() {
            topic.disable(this);
        }

//        @Override
//        public String toString() {
//
//            //return "On.weak:" + topic + "->" + reaction.get();
//        }
    }

}
