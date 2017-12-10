package nars.op.java;

import nars.NAR;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class Teacher<X> {

    final static org.slf4j.Logger logger = LoggerFactory.getLogger(Teacher.class);
    protected final X x;
    protected final NAR n;

    public Teacher(NAR n, X c) {
        this(new Opjects(n), c);
    }

    public Teacher(NAR n, Class<? extends X> c, Object... args) {
        this(new Opjects(n), c, args);
    }

    public Teacher(Opjects objs, X instance) {
        this.n = objs.nar;
        this.x = objs.the("a_" + instance.getClass().getSimpleName(), instance);
    }

    public Teacher(Opjects objs, Class<? extends X> clazz, Object... args) {
        this.n = objs.nar;
        this.x = objs.a("a_" + clazz.getSimpleName(), clazz, args);
    }

    public Trick<X> teach(String taskName,
                      Consumer<X> pre,
                      Consumer<X> task,
                      Predicate<X> post /* validation*/) {

        Trick<X> t = new Trick<>(taskName, pre, task, post);
        t.train(x, n);

        boolean valid = t.valid(x);
        if (!valid)
            throw new RuntimeException("invalid after training. please dont confuse NARS");

        n.run(1000); //debriefing

        return t;
    }
}