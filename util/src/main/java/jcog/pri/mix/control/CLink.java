package jcog.pri.mix.control;

import jcog.pri.Priority;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.roaringbitmap.RoaringBitmap;

/** Priority implementatio nwhich proxies to another and attaches a bitmap feature vector */
public class CLink<X extends Priority> extends RoaringBitmap implements Priority {

    @NotNull public final X ref;

    public CLink(@NotNull X ref, int... initialBits) {
        super();
        this.ref = ref;
        for (int i : initialBits)
            add(i);
    }

    @Override
    public String toString() {
        return ref + super.toString();
    }

    @Override
    public int hashCode() {
        return ref.hashCode();
    }

    @Override
    public boolean equals(@NotNull Object o) {
        if (this == o) return true;
        if (this.ref == o) return true;
        CLink c = (CLink)o; //assumed
        return ref.equals(c.ref);
    }

    @Override
    public float priSet(float p) {
        return ref.priSet(p);
    }

    @Override
    public @Nullable Priority clonePri() {
        return ref.clonePri();
    }

    @Override
    public float pri() {
        return ref.pri();
    }

    @Override
    public boolean delete() {
        return ref.delete();
    }

    @Override
    public boolean isDeleted() {
        return ref.isDeleted();
    }
}
