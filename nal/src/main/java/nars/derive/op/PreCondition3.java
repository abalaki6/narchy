//package nars.nal.meta.pre;
//
//import nars.nal.meta.AtomicBooleanCondition;
//import nars.nal.meta.PremiseMatch;
//import nars.term.Term;
//import org.jetbrains.annotations.NotNull;
//import org.jetbrains.annotations.Nullable;
//
///**
// * Created by me on 8/15/15.
// */
//public abstract class PreCondition3 extends AtomicBooleanCondition<PremiseMatch> {
//    @Nullable
//    public Term arg1=null, arg2=null, arg3=null;
//
//
//    /** no arguments should be null */
//    protected PreCondition3(@Nullable Term var1, @Nullable Term var2, @Nullable Term var3) {
//        arg1 = var1;
//        arg2 = var2;
//        arg3 = var3;
//    }
//
//    @Override
//    public boolean booleanValueOf(@NotNull PremiseMatch m) {
//        //these should not resolve to null
//        Term a = m.apply(arg1);
//        boolean r=false;
//        if (a != null) {
//            Term b = m.apply(arg2);
//            if (b != null) {
//                Term c = m.apply(arg3);
//                r=c != null && test(m, a, b, c);
//            }
//        }
//        return r;
//    }
//
//    public abstract boolean test(PremiseMatch m, Term a, Term b, Term c);
//
//    @NotNull
//    @Override
//    public String toString() {
//        return getClass().getSimpleName() + '[' + arg1 + ',' + arg2 + ',' + arg3 + ']';
//    }
// }
