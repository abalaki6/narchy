package nars.truth.func;

import nars.$;
import nars.NAR;
import nars.Symbols;
import nars.nal.meta.AllowOverlap;
import nars.nal.meta.SinglePremise;
import nars.term.Term;
import nars.truth.Truth;
import nars.truth.TruthFunctions;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.Map;

public enum GoalFunction implements TruthOperator {

    @SinglePremise
    Negation() {
        @Override public @Nullable Truth apply(@Nullable final Truth T, @Nullable final Truth B, NAR m, float minConf) {
            return TruthFunctions.negation(T, minConf);
        }
    },

    Strong() {
        @Nullable
        @Override public Truth apply(@Nullable final Truth T, @Nullable final Truth B, NAR m, float minConf) {
            return (T == null || B == null) ? null : TruthFunctions.desireStrong(T, B, minConf);
        }
    },

    Weak() {
        @Nullable
        @Override public Truth apply(@Nullable final Truth T, @Nullable final Truth B, NAR m, float minConf) {
            return (T == null || B == null) ? null : TruthFunctions.desireWeak(T, B, minConf);
        }
    },

    Induction() {
        @Nullable
        @Override public Truth apply(@Nullable final Truth T, @Nullable final Truth B, NAR m, float minConf) {
            return B == null ? null : TruthFunctions.desireInd(T, B, minConf);
        }
    },

    //@AllowOverlap
    Deduction() {
        @Nullable
        @Override public Truth apply(@Nullable final Truth T, @Nullable final Truth B, NAR m, float minConf) {
            return (T == null || B == null) ? null : TruthFunctions.desireDed(T, B, minConf);
        }
    },

//    //EXPERIMENTAL
//    Abduction() {
//        @Nullable
//        @Override public Truth apply(@Nullable final Truth T, @Nullable final Truth B, @NotNull Memory m, float minConf) {
//            return ((B == null) || (T == null)) ? null : TruthFunctions.abduction(T, B, minConf);
//        }
//    },



    @SinglePremise
    Identity() {
        @Nullable
        @Override public Truth apply(@Nullable final Truth T, @Nullable final Truth B, NAR m, float minConf) {
            return TruthOperator.identity(T, minConf);
        }
    },

    /** same as identity but allows overlap */
    @SinglePremise
    @AllowOverlap
    IdentityTransform() {
        @Nullable
        @Override public Truth apply(@Nullable final Truth T, @Nullable final Truth B, NAR m, float minConf) {
            return TruthOperator.identity(T, minConf);
        }
    },

//    @AllowOverlap @SinglePremise
//    StructuralStrong() {
//        @Nullable
//        @Override public Truth apply(@Nullable final Truth T, @Nullable final Truth B, NAR m, float minConf) {
//            return TruthFunctions.desireStrong(T, defaultTruth(m), minConf);
//        }
//    },

    @SinglePremise
    @AllowOverlap
    StructuralDeduction() {
        @Nullable
        @Override public Truth apply(@Nullable final Truth T, final Truth B, NAR m, float minConf) {
            return T != null ? TruthFunctions.deduction1(T, defaultTruth(m).conf(), minConf) : null;
        }
    },

    BeliefStructuralDeduction() {
        @Nullable
        @Override public Truth apply(final Truth T, @Nullable final Truth B, NAR m, float minConf) {
            if (B == null) return null;
            return TruthFunctions.deduction1(B, defaultConfidence(m), minConf);
        }
    },


//    @AllowOverlap @SinglePremise
//    StructuralStrongNeg() {
//        @Nullable
//        @Override public Truth apply(@Nullable final Truth T, @Nullable final Truth B, @NotNull Memory m, float minConf) {
//            return TruthFunctions.desireStrong(T, defaultTruth(m).negated(), minConf);
//        }
//    },


    Union() {
        @Nullable
        @Override public Truth apply(@Nullable final Truth T, @Nullable final Truth B, NAR m, float minConf) {
            return ((B == null) || (T == null)) ? null : TruthFunctions.union(T, B, minConf);
        }
    },


    Intersection() {
        @Nullable
        @Override public Truth apply(@Nullable final Truth T, @Nullable final Truth B, NAR m, float minConf) {
            return ((B == null) || (T == null)) ? null : TruthFunctions.intersection(T, B, minConf);
        }
    },

    Difference() {
        @Nullable
        @Override public Truth apply(@Nullable final Truth T, @Nullable final Truth B, NAR m, float minConf) {
            return ((B == null) || (T == null)) ? null : TruthFunctions.difference(T, B, minConf);
        }
    },


    ;

    @Nullable
    private static Truth defaultTruth(NAR m) {
        return m.truthDefault(Symbols.GOAL /* goal? */);
    }


    static final Map<Term, TruthOperator> atomToTruthModifier = $.newHashMap(GoalFunction.values().length);

    static {
        TruthOperator.permuteTruth(GoalFunction.values(), atomToTruthModifier);
    }


    public static TruthOperator get(Term a) {
        return atomToTruthModifier.get(a);
    }


    private final boolean single;
    private final boolean overlap;

    GoalFunction() {

        try {
            Field enumField = getClass().getField(name());
            this.single = enumField.isAnnotationPresent(SinglePremise.class);
            this.overlap = enumField.isAnnotationPresent(AllowOverlap.class);
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

    private static float defaultConfidence(NAR m) {
        return m.confidenceDefault(Symbols.GOAL);
    }
}