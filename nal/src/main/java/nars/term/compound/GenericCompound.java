package nars.term.compound;

import jcog.Util;
import nars.IO;
import nars.Op;
import nars.term.Compound;
import nars.term.container.TermContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static nars.time.Tense.DTERNAL;


public class GenericCompound implements Compound {

    /**
     * subterm vector
     */
    @NotNull
    protected final TermContainer subterms;


    /**
     * content hash
     */
    public final int hash;

    @NotNull
    public final Op op;


    public transient boolean normalized;


    public GenericCompound(@NotNull Op op, @NotNull TermContainer subterms) {

        this.op = op;

        this.subterms = subterms;

        this.hash = Util.hashCombine(subterms.hashCode(), op.ordinal());

        this.normalized = !(vars() > 0 || varPattern() > 0);

//        //HACK
//        this.dynamic =
//                (op() == INH && subOpIs(1,ATOM) && subOpIs(0, PROD)) /* potential function */
//                        ||
//                (hasAll(EvalBits) && OR(Termlike::isDynamic)); /* possible function in subterms */
    }


//    @Override
//    public boolean isDynamic() {
//        return dynamic;
//    }

    @NotNull
    @Override
    public final TermContainer subterms() {
        return subterms;
    }


    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public final boolean isNormalized() {
        return normalized;
    }


    @Override
    public int dt() {
        return DTERNAL;
    }

    /**
     * do not call this manually, it will be set by VariableNormalization only
     */
    @Override
    public final void setNormalized() {
        this.normalized = true;
    }

    @Override
    public boolean isCommutative() {
        return op().commutative && size() > 1;
    }

    @NotNull
    @Override
    public final Op op() {
        return op;
    }

    @NotNull
    @Override
    public String toString() {
        return IO.Printer.stringify(this).toString();
    }

    @Override
    public final boolean equals(@Nullable Object that) {
        return Compound.equals(this, that);
    }

    /** creates a new instance with the given dt value */
    @Override
    public Compound dt(int dt) {
        if (dt==DTERNAL)
            return this;
        else {
            if (op.temporal || op.image)
                return new GenericCompoundDT(this, dt);
            else
                return this; //maybe detect when this happens, could indicate an error
        }
    }

}
