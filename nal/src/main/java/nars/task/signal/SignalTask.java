package nars.task.signal;


import nars.task.NALTask;
import nars.term.Term;
import nars.truth.Truth;


public class SignalTask extends NALTask {

    public SignalTask(Term t, byte punct, Truth truth, long start, long end, long stamp) {
        super(t, punct, truth, start, start, end,
                new long[]{stamp} /* TODO use an implementation which doenst need an array for this */);
    }

    @Override
    public float eternalizable() {
        return 0;
        //return 0.5f;
        //return 0.1f;
    }




}
