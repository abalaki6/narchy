//package nars.link;
//
//import nars.budget.Budget;
//import nars.budget.Budgeted;
//import org.jetbrains.annotations.NotNull;
//
//import static nars.budget.Budget.validPriority;
//
///**
// * Created by me on 9/6/16.
// */
//@Deprecated public class ArrayBLink<X> implements BLink<X> {
//
//    public X id;
//    public float[] f;
//
//    public ArrayBLink() {
//
//    }
//
//    public ArrayBLink(X id, float[] f) {
//        this.id = id;
//        this.f = f;
//    }
//
//
//    @Override
//    public final X get() {
//        return id;
//    }
//
//
//    @NotNull
//    @Override
//    public Budget setBudget(float p, float q) {
//        f[0] = validPriority(p);
//        f[1] = Budget.validQuality(q);
//        return this;
//    }
//
//    @Override
//    public final void setPriority(float p) {
//        f[0] = validPriority(p);
//    }
//
//
//    @Override
//    public final void setQuality(float q) {
//        f[1] = Budget.validQuality(q);
//    }
//
//    @Override
//    public final @NotNull Budget clone() {
//        throw new UnsupportedOperationException();
//    }
//
//
//
//
//    @Override
//    public final boolean delete() {
//        float p = f[0];
//        if (p != p) {
//            f[0] = Float.NaN;
//            return true;
//        }
//        return false;
//    }
//
//    @Override
//    public float pri() {
//        return f[0];
//    }
//
//    @Override
//    public final float qua() {
//        return f[1];
//    }
//
//    @NotNull
//    public ArrayBLink<X> set(X id, float[] v) {
//        this.id = id;
//        this.f = v;
//        return this;
//    }
//
//    @Override
//    public @NotNull String toString() {
//        return id + "=" + toBudgetString();
//    }
//
//    public static class ArrayBLinkToBudgeted<X extends Budgeted> extends ArrayBLink<X> {
//
//        public ArrayBLinkToBudgeted(X id, float[] f) {
//            super(id, f);
//        }
//
//        @Override
//        public float pri() {
//            if (id.isDeleted()) {
//                delete();
//                return Float.NaN;
//            }
//            return super.pri();
//        }
//    }
//}