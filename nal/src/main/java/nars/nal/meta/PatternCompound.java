package nars.nal.meta;

import nars.Op;
import nars.nal.meta.match.Ellipsis;
import nars.nal.meta.match.EllipsisTransform;
import nars.term.Compound;
import nars.term.Term;
import nars.term.compound.GenericCompound;
import nars.term.container.TermContainer;
import nars.term.container.TermVector;
import nars.term.subst.FindSubst;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

abstract public class PatternCompound extends GenericCompound {

    public final int sizeCached;
    public final int volCached;
    public final int structureCached;
    public final boolean imgCached;

    @NotNull
    public final Term[] termsCached;


    PatternCompound(@NotNull Compound seed, @NotNull TermContainer subterms) {
        super(seed.op(), seed.dt(), subterms);

        if (seed.isNormalized())
            this.setNormalized();

        sizeCached = seed.size();
        structureCached =
                //seed.structure() & ~(Op.VariableBits);
                seed.structure() & ~(Op.VAR_PATTERN.bit);
        this.volCached = seed.volume();
        this.termsCached = subterms.terms();
        this.imgCached = op.isImage();

    }


    abstract protected static class PatternCompoundWithEllipsis extends PatternCompound {

        @Nullable
        protected final Ellipsis ellipsis;

        PatternCompoundWithEllipsis(@NotNull Compound seed, @Nullable Ellipsis ellipsis, @NotNull TermContainer subterms) {
            super(seed, subterms);

            this.ellipsis = ellipsis;
            if (ellipsis == null)
                throw new RuntimeException("no ellipsis");


        }

        abstract protected boolean matchEllipsis(@NotNull Compound y, @NotNull FindSubst subst);

        protected boolean canMatch(@NotNull Compound y) {
            int yStructure = y.structure();
            return ((yStructure | structureCached) == yStructure);
        }

        @Override
        public boolean match(@NotNull Compound y, @NotNull FindSubst subst) {
            return canMatch(y) && matchEllipsis(y, subst);
        }


    }


    public static class PatternCompoundWithEllipsisLinear extends PatternCompoundWithEllipsis {

        public PatternCompoundWithEllipsisLinear(@NotNull Compound seed, @Nullable Ellipsis ellipsis, @NotNull TermContainer subterms) {
            super(seed, ellipsis, subterms);
        }

        @Override
        protected boolean matchEllipsis(@NotNull Compound y, @NotNull FindSubst subst) {
            return subst.matchCompoundWithEllipsisLinear(
                    this, ellipsis, y
            );
        }

    }


    /**
     * requies dt exact match, for example, when matching Images (but not temporal terms)
     */
    public static final class PatternCompoundWithEllipsisLinearDT extends PatternCompoundWithEllipsisLinear {

        public PatternCompoundWithEllipsisLinearDT(@NotNull Compound seed, @Nullable Ellipsis ellipsis, @NotNull TermContainer subterms) {
            super(seed, ellipsis, subterms);
        }

        @Override
        protected boolean canMatch(@NotNull Compound y) {
            return (dt == y.dt() && super.canMatch(y));
        }


    }


    public static final class PatternCompoundWithEllipsisCommutive extends PatternCompoundWithEllipsis {

        public PatternCompoundWithEllipsisCommutive(@NotNull Compound seed, @Nullable Ellipsis ellipsis, @NotNull TermContainer subterms) {
            super(seed, ellipsis, subterms);
        }

        @Override
        protected boolean matchEllipsis(@NotNull Compound y, @NotNull FindSubst subst) {
            return subst.matchEllipsedCommutative(
                    this, ellipsis, y
            );
        }

    }

    public static final class PatternCompoundSimple extends PatternCompound {

        public PatternCompoundSimple(@NotNull Compound seed, @NotNull TermContainer subterms) {
            super(seed, subterms);
        }

        @Override
        public boolean match(@NotNull Compound y, @NotNull FindSubst subst) {
            @NotNull TermVector subterms = this.subterms;
            if (canMatch(y)) {
                TermContainer ysubs = y.subterms();
                return ((y.isCommutative()) ?
                        subst.matchPermute(subterms, ysubs) :
                        subst.matchLinear(subterms, ysubs));
            }
            return false;
        }

        protected final boolean canMatch(@NotNull Compound y) {

            int yStructure = y.structure();

            return ((structureCached | yStructure) == yStructure) &&
                    (sizeCached == y.size()) &&
                    (volCached <= y.volume()) &&
                    (!imgCached || /*image &&*/ (dt == y.dt()));
        }


    }



//    PatternCompound(@NotNull Compound seed) {
//        this(seed, (TermVector) seed.subterms());
//    }


    @NotNull
    @Override
    public Term[] terms() {
        return termsCached;
    }

    @Override
    public final int structure() {
        return structureCached;
    }

    @Override
    abstract public boolean match(@NotNull Compound y, @NotNull FindSubst subst);
    //abstract protected boolean canMatch(@NotNull Compound y);


}
/**
 * Created by me on 12/26/15.
 */ //    public static class VariableDependencies extends DirectedAcyclicGraph<Term,String> {
//
//
//        public Op type;
//
//        /* primary ==> secondary */
//        protected void dependency(Term primary, Term secondary) {
//            addVertex(primary);
//            addVertex(secondary);
//            try {
//                addDagEdge(primary, secondary, "d" + edgeSet().size()+1);
//            } catch (CycleFoundException e) {
//                //System.err.println(e);
//            }
//        }
//
//
//        public static class PatternVariableIndex extends VarPattern {
//
//            public final Compound parent;
//            public final int index;
//
//            public PatternVariableIndex(String id, Compound parent, int index) {
//                super(id);
//                this.parent = parent;
//                this.index = index; //first index
//            }
//            public PatternVariableIndex(Variable v, Compound parent) {
//                this(v.id, parent, parent.indexOf(v));
//            }
//
//            public String toString() {
//                return super.toString() + " @ " + parent + " index " + index;
//            }
//        }
//
//        public static class RematchedPatternVariableIndex extends PatternVariableIndex {
//
//            public RematchedPatternVariableIndex(PatternVariableIndex i) {
//                super(i.id + "_", i.parent, i.index);
//            }
//        }
//
//
//        final Map<Variable,PatternVariableIndex> variables = Global.newHashMap();
//
//        public VariableDependencies(Compound c, Op varType) {
//            super(null);
//
//            this.type = varType;
//
//            c.recurseTerms( (s, p) -> {
//                boolean existed = !addVertex(s);
//
//                if (p == null)
//                    return; //top level
//
//                addVertex(p);
//
//                //if (t instanceof Compound) {
//
//                //compoundIn.put((Compound)p, (Compound)t);
//
//                if (s.op(varType)) {
//                    if (existed) {
//                        PatternVariableIndex s0 = variables.get(s);
//                        s = new RematchedPatternVariableIndex(s0); //shadow variable dependent
//                        //dependency(s0, s); //variable re-use after first match
//
//                        //compound depends on existing variable
//                        dependency(s0, p);
//                        dependency(p, s);
//                    } else {
//                        //variable depends on existing compound
//                        PatternVariableIndex ss = new PatternVariableIndex((Variable) s, (Compound) p);
//                        variables.put((Variable) s, ss);
//                        dependency(p, ss);
//                    }
//                }
//                else {
//                    if (s.isCommutative()) {
//                        //term is commutive
//                        //delay commutive terms to the 2nd stage
//                        dependency(Op.Imdex, s);
//                    } else {
//
//                        //term depends on existing compound
//                        dependency(p, s);
//                    }
//                }
////                }
////                else {
////                    if (!t.op(varType)) return;
////
////                    varIn.put((Variable) t, (Compound) p);
////                    compHas.put((Compound)p, (Variable)t);
////
////                    try {
////                        addDagEdge(p, t,  "requries(" + t + "," + p + ")");
////                    } catch (Exception e1) {
////                        System.err.println(e1);
////                    }
////                }
//            });
//
//            Term last = null;
//            //DepthFirstIterator ii = new DepthFirstIterator(this, c);
//            Iterator ii = iterator(); //topological
//            while (ii.hasNext()) last = (Term) ii.next();
//
//            //second stage as a shadow node
//            dependency(last, Op.Imdex);
//
//
//
//        }
//    }
//
