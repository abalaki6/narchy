package nars.truth;

import nars.$;
import nars.Global;
import nars.Memory;
import nars.Symbols;
import nars.nal.meta.AllowOverlap;
import nars.nal.meta.SinglePremise;
import nars.nal.meta.TruthOperator;
import nars.term.Term;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * http://aleph.sagemath.org/?q=qwssnn
 <patham9> only strong rules are allowing overlap
 <patham9> except union and revision
 <patham9> if you look at the graph you see why
 <patham9> its both rules which allow the conclusion to be stronger than the premises
 */
public enum BeliefFunction implements TruthOperator {

//    @SinglePremise
//    Revision() {
//        @Nullable
//        @Override public Truth apply(final Truth T, final Truth B, @NotNull Memory m, float minConf) {
//            return ((B == null) || (T == null)) ? null : TruthFunctions.revision(T, B, 1f, -1f);
//        }
//    },

    StructuralIntersection() {
        @Nullable
        @Override public Truth apply(final Truth T, final Truth B, @NotNull Memory m, float minConf) {
            if (B == null) return null;
            return TruthFunctions.intersection(B, defaultTruth(m), minConf);
        }
    },

    @SinglePremise @AllowOverlap
    StructuralDeduction() {
        @NotNull
        @Override public Truth apply(final Truth T, final Truth B, @NotNull Memory m, float minConf) {
            return (T == null) ? null : TruthFunctions.deduction1(T, defaultConfidence(m), minConf);
        }
    },

    StructuralAbduction() {
        @Nullable
        @Override public Truth apply(final Truth T, final Truth B, @NotNull Memory m, float minConf) {
            return (B != null) ? TruthFunctions.abduction(B, defaultTruth(m), minConf) : null;
        }
    },

    Deduction() {
        @Nullable
        @Override public Truth apply(final Truth T, final Truth B, @NotNull Memory m, float minConf) {
            return ((B == null) || (T == null)) ? null : TruthFunctions.deduction(T, B, minConf);
        }
    },

    Induction() {
        @Nullable
        @Override public Truth apply(final Truth T, final Truth B, @NotNull Memory m, float minConf) {
            return ((B == null) || (T == null)) ? null : TruthFunctions.induction(T, B, minConf);
        }
    },

    @AllowOverlap
    InductionOverlappable() {
        @Nullable
        @Override public Truth apply(final Truth T, final Truth B, @NotNull Memory m, float minConf) {
            return ((B == null) || (T == null)) ? null : TruthFunctions.induction(T, B, minConf);
        }
    },

    Abduction() {
        @Nullable
        @Override public Truth apply(final Truth T, final Truth B, @NotNull Memory m, float minConf) {
            return ((B == null) || (T == null)) ? null : TruthFunctions.abduction(T, B, minConf);
        }
    },

    Comparison() {
        @Nullable
        @Override public Truth apply(final Truth T, final Truth B, @NotNull Memory m, float minConf) {
            return ((B == null) || (T == null)) ? null : TruthFunctions.comparison(T, B, minConf);
        }
    },

    Conversion() {
        @Nullable
        @Override public Truth apply(final Truth T, final Truth B, @NotNull Memory m, float minConf) {
            return (B == null) ? null : TruthFunctions.conversion(B, minConf);
        }
    },

    @SinglePremise
    Negation() {
        @NotNull
        @Override public Truth apply(final Truth T, /* nullable */ final Truth B, @NotNull Memory m, float minConf) {
            return (T == null) ? null : TruthFunctions.negation(T, minConf);
        }
    },

    @SinglePremise
    Contraposition() {
        @Nullable
        @Override public Truth apply(final Truth T, /* nullable */ final Truth B, @NotNull Memory m, float minConf) {
            return (T == null) ? null : TruthFunctions.contraposition(T, minConf);
        }
    },

    @AllowOverlap
    Resemblance() {
        @Nullable
        @Override public Truth apply(final Truth T, final Truth B, @NotNull Memory m, float minConf) {
            return ((B == null) || (T == null)) ? null : TruthFunctions.resemblance(T, B, minConf);
        }
    },

    Union() {
        @Nullable
        @Override public Truth apply(final Truth T, final Truth B, @NotNull Memory m, float minConf) {
            if (B == null || T == null) return null;
            return TruthFunctions.union(T,B,minConf);
        }
    },

    //@AllowOverlap
    Intersection() {
        @Nullable
        @Override public Truth apply(@Nullable final Truth T, final Truth B, @NotNull Memory m, float minConf) {
            if (B == null || T == null) return null;
            return TruthFunctions.intersection(T,B,minConf);
        }
    },

    @AllowOverlap
    Difference() {
        @Nullable
        @Override public Truth apply(@Nullable final Truth T, final Truth B, @NotNull Memory m, float minConf) {
            return ((B == null) || (T == null)) ? null : TruthFunctions.difference(T, B, minConf);
        }
    },

    @AllowOverlap
    Analogy() {
        @Nullable
        @Override public Truth apply(final Truth T, final Truth B, @NotNull Memory m, float minConf) {
            if (B == null || T == null) return null;
            return TruthFunctions.analogy(T,B,minConf);
        }
    },
    ReduceConjunction() {
        @Nullable
        @Override public Truth apply(final Truth T, final Truth B, @NotNull Memory m, float minConf) {
            if (B == null || T == null) return null;
            return TruthFunctions.reduceConjunction(T,B,minConf);
        }
    },
    ReduceDisjunction() {
        @Nullable
        @Override public Truth apply(final Truth T, final Truth B, @NotNull Memory m, float minConf) {
            if (B == null || T == null) return null;
            return TruthFunctions.reduceDisjunction(T, B,minConf);
        }
    },
    ReduceConjunctionNeg() {
        @Nullable
        @Override public Truth apply(final Truth T, final Truth B, @NotNull Memory m, float minConf) {
            if (B == null || T == null) return null;
            return TruthFunctions.reduceConjunctionNeg(T, B,minConf);
        }
    },
    AnonymousAnalogy() {
        @Nullable
        @Override public Truth apply(final Truth T, final Truth B, @NotNull Memory m, float minConf) {
            if (B == null || T == null) return null;
            return TruthFunctions.anonymousAnalogy(T,B,minConf);
        }
    },
    Exemplification() {
        @Nullable
        @Override public Truth apply(final Truth T, final Truth B, @NotNull Memory m, float minConf) {
            if (B == null || T == null) return null;
            return TruthFunctions.exemplification(T, B, minConf);
        }
    },
    DecomposeNegativeNegativeNegative() {
        @Nullable
        @Override public Truth apply(final Truth T, final Truth B, @NotNull Memory m, float minConf) {
            if (B == null || T == null) return null;
            return TruthFunctions.decomposeNegativeNegativeNegative(T, B, minConf);
        }
    },
    DecomposePositiveNegativePositive() {
        @Nullable
        @Override public Truth apply(final Truth T, final Truth B, @NotNull Memory m, float minConf) {
            if (B == null || T == null) return null;
            return TruthFunctions.decomposePositiveNegativePositive(T,B);
        }
    },
    DecomposeNegativePositivePositive() {
        @Nullable
        @Override public Truth apply(final Truth T, final Truth B, @NotNull Memory m, float minConf) {
            if (B == null || T == null) return null;
            return TruthFunctions.decomposeNegativePositivePositive(T,B);
        }
    },
    DecomposePositivePositivePositive() {
        @Nullable
        @Override public Truth apply(final Truth T, final Truth B, @NotNull Memory m, float minConf) {
            if (B == null || T == null) return null;
            Truth Tn = TruthFunctions.negation(T, minConf);
            return (Tn == null) ? null : TruthFunctions.decomposeNegativePositivePositive(Tn, B);
        }
    },
    DecomposePositiveNegativeNegative() {
        @Nullable
        @Override public Truth apply(final Truth T, final Truth B, @NotNull Memory m, float minConf) {
            if (B == null || T == null) return null;
            return TruthFunctions.decomposePositiveNegativeNegative(T,B);
        }
    },

    @SinglePremise
    Identity() {
        @NotNull
        @Override public Truth apply(final Truth T,  final Truth B, @NotNull Memory m, float minConf) {
            return (T==null || (T.conf() < minConf)) ? null : T;
            //return new DefaultTruth(T.freq(), T.conf());
        }
    },

    BeliefIdentity() {
        @Nullable
        @Override public Truth apply(final Truth T,  final Truth B, @NotNull Memory m, float minConf) {
            //if (B == null) return null;
            //return new DefaultTruth(B.freq(), B.conf());
            return (B == null || (B.conf() < minConf)) ? null : B;
        }
    },

    @AllowOverlap
    BeliefStructuralDeduction() {
        @Nullable
        @Override public Truth apply(final Truth T,  final Truth B, @NotNull Memory m, float minConf) {
            if (B == null) return null;
            return TruthFunctions.deduction1(B, defaultConfidence(m), minConf);
        }
    },

    @AllowOverlap
    BeliefStructuralDifference() {
        @Nullable
        @Override public Truth apply(final Truth T,  final Truth B, @NotNull Memory m, float minConf) {
            if (B == null) return null;
            Truth res =  TruthFunctions.deduction1(B, defaultConfidence(m), minConf);
            return (res != null) ? new DefaultTruth(1.0f - res.freq(), res.conf()) : null;
        }
    },
    BeliefNegation() {
        @Nullable
        @Override public Truth apply(final Truth T,  final Truth B, @NotNull Memory m, float minConf) {
            return (B == null) ? null : TruthFunctions.negation(B, minConf);
        }
    };


    @NotNull
    public static Truth defaultTruth(@NotNull Memory m) {
        return m.getTruthDefault(Symbols.BELIEF);
    }

    @NotNull
    public static float defaultConfidence(@NotNull Memory m) {
        return m.getDefaultConfidence(Symbols.BELIEF);
    }



//    /**
//     * @param T taskTruth
//     * @param B beliefTruth (possibly null)
//     * @return
//     */
//    @Override
//    abstract public Truth apply(Truth T, Truth B, @NotNull Memory m);



    //TODO use an enum map with terms bound to the enum values directly
    static final Map<Term, BeliefFunction> atomToTruthModifier = Global.newHashMap(BeliefFunction.values().length);

    static {
        for (BeliefFunction tm : BeliefFunction.values())
            atomToTruthModifier.put($.the(tm.toString()), tm);
    }

    @Nullable
    public static BeliefFunction get(Term a) {
        return atomToTruthModifier.get(a);
    }


    private final boolean single;
    private final boolean overlap;

    BeliefFunction() {

        try {
            Field enumField = getClass().getField(name());
            this.single = enumField.isAnnotationPresent(SinglePremise.class);
            this.overlap = this.single || enumField.isAnnotationPresent(AllowOverlap.class);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }


    }

    @Override
    public boolean single() {
        return single;
    }

    @Override
    public boolean allowOverlap() {
        return overlap;
    }
}
