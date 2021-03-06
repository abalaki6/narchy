package jcog.bag.util;


/** TODO make the bit which it checks adjustable for chaining these */
public class Treadmill2 implements SpinMutex {

    final SpinMutex a, b;
    private final int cHalf;

    public Treadmill2() {
        this(Runtime.getRuntime().availableProcessors());
    }
    public Treadmill2(int concurrency) {
        cHalf = Math.max(1, concurrency/2);
        a = new Treadmill( cHalf );
        b = new Treadmill( cHalf );
    }

    private SpinMutex select(long hash) {
        return (hash & 1) == 0 ? a : b;
    }

    @Override
    public int start(long hash) {
        SpinMutex x = select(hash);
        int i = x.start(hash);
        if (x == b)
            i += cHalf;
        return i;
    }

    @Override
    public void end(int slot) {
        SpinMutex which;
        if (slot >= cHalf) {
            slot -= cHalf;
            which = b;
        } else {
            which = a;
        }
        which.end(slot);
    }
}
