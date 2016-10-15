package nars.index.term;

import nars.*;
import nars.Op;
import nars.concept.Concept;
import nars.concept.util.ConceptBuilder;
import nars.concept.util.InvalidConceptException;
import nars.nal.TermBuilder;
import nars.nal.meta.PremiseEval;
import nars.nal.meta.match.EllipsisMatch;
import nars.term.*;
import nars.term.atom.Atomic;
import nars.term.compound.GenericCompound;
import nars.term.compound.ProtoCompound;
import nars.term.container.TermContainer;
import nars.term.container.TermVector;
import nars.term.subst.MapSubst;
import nars.term.subst.Subst;
import nars.term.transform.CompoundTransform;
import nars.term.transform.TermTransform;
import nars.term.transform.VariableNormalization;
import nars.util.data.map.nbhm.HijacKache;
import org.eclipse.collections.api.list.primitive.ByteList;
import org.eclipse.collections.impl.factory.Maps;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import static nars.Op.*;
import static nars.term.Term.False;
import static nars.term.Termed.termOrNull;
import static nars.term.Terms.compoundOrNull;
import static nars.time.Tense.DTERNAL;

/**
 *
 */
public abstract class TermIndex extends TermBuilder {


    public static final Logger logger = LoggerFactory.getLogger(TermIndex.class);


    /**
     * get if not absent
     */
    @Nullable
    public final Termed get(@NotNull Term t) {
        return get(t, false);
    }

    @Nullable
    public abstract Termed get(@NotNull Term key, boolean createIfMissing);

    /**
     * sets or replaces the existing value, unless the existing value is a PermanentConcept it must not
     * be replaced with a non-Permanent concept
     */
    public abstract void set(@NotNull Term src, Termed target);

    public final void set(@NotNull Termed t) {
        set(t.term(), t);
    }


    abstract public void clear();

    abstract public void forEach(Consumer<? super Termed> c);


    public void start(NAR nar) {
    }

    /**
     * # of contained terms
     */
    public abstract int size();

    @Nullable
    abstract public ConceptBuilder conceptBuilder();

    /**
     * override to possibly intern termcontainers
     */
    public TermContainer intern(TermContainer s) {
        return s;
    }


    /**
     * a string containing statistics of the index's current state
     */
    @NotNull
    public abstract String summary();

    public abstract void remove(@NotNull Term entry);


    public final HijacKache<TermContainer, TermContainer> normalizations =
            new HijacKache<>(Param.NORMALIZATION_CACHE_SIZE, 4);
    public final HijacKache<ProtoCompound, Term> terms =
            new HijacKache<>(Param.TERM_CACHE_SIZE, 4);

//    final ThreadLocal<Map<Compound,Compound>> normalizations =
//            ThreadLocal.withInitial( () ->
//                new CapacityLinkedHashMap(Param.NORMALIZATION_CACHE_SIZE_PER_THREAD)
//            );
//            //Collections.synchronizedMap( new CapacityLinkedHashMap(16*1024) );
    //final Cache<Compound,Compound> normalizations = Caffeine.newBuilder().maximumSize(Param.NORMALIZATION_CACHE_SIZE).build();


    final Function<? super ProtoCompound, ? extends Term> termizer = pc -> {

        try {
            return super.the(pc.op(), pc.dt(), pc.terms());
        } catch (InvalidTermException x) {
            //if (Param.DEBUG_EXTRA)
            logger.info("Termizer: {} {}", pc, x);
            return False; //place a False placeholder so that a repeat call will not have to discover this manually
        } catch (Throwable e) {
            logger.error("Termizer: {} {}", pc, e);
            return False;
        }
    };

    @NotNull
    public final Term cached(@NotNull Op op, int dt, @NotNull Term[] u) throws InvalidTermException {

        if (cacheable(op, u)) {

            ProtoCompound p = ProtoCompound.the(op, dt, u);


            Term t = terms.computeIfAbsent(p, termizer);

//            if (failure[0] != null) {
//                Throwable f = failure[0];
//                if (f instanceof InvalidTermException)
//                    throw ((InvalidTermException) f);
//                throw new RuntimeException(failure[0]);
//            }

//            //SANITY TEST:
//            @NotNull Term retry = super.the(p.op(), p.dt(), p.terms());
//            if (!t.equals(retry)) {
//                terms.computeIfAbsent(p, termizer);
//                throw new RuntimeException("cache fault");
//            }

            return t;

        } else {
            return super.the(op, dt, u);
        }
    }


    /**
     * returns the resolved term according to the substitution
     */
    @Nullable
    public Term transform(@NotNull Term src, @NotNull Subst f) {


        Term y = f.xy(src);
        if (y != null)
            return y; //an assigned substitution, whether a variable or other type of term

        Op op = src.op();

        if (src instanceof Atomic) {

            if (op.var) {
                if (op == VAR_PATTERN)
                    return null; //unassigned pattern variable
                else
                    return src; //unassigned but literal non-pattern var
            } else {

                Termed existing = get(src); //reresolve
                if (existing != null)
                    return existing.term();
                else
                    return src;
            }
        }


        //no variables that could be substituted, so return this constant
        if (f instanceof PremiseEval && src.vars() + src.varPattern() == 0) //shortcut for premise evaluation matching
            return src;

        int len = src.size();
        List<Term> sub = $.newArrayList(len /* estimate */);

        boolean strict = f instanceof PremiseEval;

        boolean changed = false;
        Compound crc = (Compound) src;
        Term[] cct = crc.terms();
        for (int i = 0; i < len; i++) {
            Term t = cct[i];
            Term u = transform(t, f);


            if (u instanceof EllipsisMatch) {

                ((EllipsisMatch) u).expand(op, sub);
                changed = true;

            } else {

                if (u == null) {

                    if (strict)
                        return null;

                    sub.add(t); //keep value

                } else {
                    sub.add(u);
                    changed |= (u != t);
                }

            }
        }

        return changed ? the(crc, sub) : crc;
    }

    @NotNull
    public final Term the(@NotNull Compound csrc, @NotNull TermContainer newSubs) {
        if (csrc.subterms().equals(newSubs)) {
            return csrc;
        } else {
            return the(csrc, newSubs.terms());
        }
    }

    @NotNull
    public final Term the(@NotNull Compound csrc, @NotNull Term[] args) {
        return the(csrc.op(), csrc.dt(), args);
    }

    @Override
    public final @NotNull Term the(@NotNull Op op, int dt, @NotNull Term[] args) throws InvalidTermException {
        return cached(op, dt, args);
    }

    @Deprecated
    public final @NotNull Term the(@NotNull Op op, @NotNull Term... tt) {
        return the(op, DTERNAL, tt); //call this implementation's, not super class's
    }

    private static boolean cacheable(Op op, Term[] u) {
        if (u.length < 2) {
            return false; //probably not worth caching small compounds
        }
        if (op == INH) {

            Term u1 = u[1];
            if (u1 instanceof TermTransform && u[0].op() == PROD) //prevents caching for potential transforming terms
                return false;

        }
        return true;
    }


    public void print(@NotNull PrintStream out) {
        forEach(out::println);
        out.println();
    }

    final Function<? super TermContainer, ? extends TermContainer> normalizer = u -> {

        TermContainer result;

        try {
            int numVars = u.vars();

            @NotNull Term[] src = u.terms();

            Term[] tgt =
                    transform(src, null,
                            (numVars == 1 && u.varPattern() == 0) ?
                                    VariableNormalization.singleVariableNormalization :
                                    new VariableNormalization(numVars /* estimate */)
                    );

            result = tgt != src ? TermVector.the(tgt) : u;

        } catch (InvalidTermException e) {

            if (Param.DEBUG_EXTRA)
                logger.warn("normalize {} : {}", u, e);

            result = InvalidSubterms;
        }

        return result;
    };

    @Nullable
    public final Compound normalize(@NotNull Compound t) {

        Compound c;

        if (t.isNormalized()) {
            c = t; //already normalized
        } else {
            //see if subterms need change
            TermContainer src = t.subterms();
            TermContainer tgt = normalize(src);
            if (src == tgt) {
                c = t; //subterms dont change
            } else if (tgt != InvalidSubterms) {
                c = Terms.compoundOrNull($.terms.the(t, tgt));
            } else {
                c = null;
            }
        }

        //if (c!=null) {
        //c = compoundOrNull($.unneg((Compound) c));
        if (c != null) {
            ((GenericCompound) c).setNormalized();
        }
        //}
        return c;
    }

    @Nullable
    public final TermContainer normalize(@NotNull TermContainer t) {
        return normalizations.computeIfAbsent(t, normalizer);
    }


    @Nullable
    public Term the(@NotNull Compound src, @NotNull List<Term> newSubs) {
        return the(src, newSubs.toArray(new Term[newSubs.size()]));
    }


    @NotNull
    public Term transform(@NotNull Compound src, @NotNull CompoundTransform t) {
        if (src == null || !t.testSuperTerm(src))
            return src;

        Term[] srcSubs = src.terms();
        Term[] tgtSubs = transform(srcSubs, src, t);

        return tgtSubs != srcSubs ?
                the(src.op(), src.dt(), tgtSubs) : //must not allow subterms to be tested for equality, for variable normalization purpose the variables will seem equivalent but they are not
                src;

    }

    @NotNull
    public Term[] transform(@NotNull Term[] src, Compound superterm, @NotNull CompoundTransform t) {

        int modifications = 0;

        Term[] target = src.clone();

        for (int i = 0, n = src.length; i < n; i++) {

            Term x = src[i], y;

            if (t.test(x)) {
                y = t.apply(superterm, x);
            } else if (x instanceof Compound) {
                y = transform((Compound) x, t); //recurse
            } else {
                continue;
            }

            if (x != y) { //must be refernce equality test for some variable normalization cases
                modifications++;
                target[i] = y;
            }

        }

        return modifications > 0 ? target : src;
    }


    @Nullable
    public Term transform(@NotNull Compound src, @NotNull ByteList path, Term replacement) {
        return transform(src, path, 0, replacement);
    }

    @Nullable
    public Term transform(@NotNull Term src, @NotNull ByteList path, int depth, Term replacement) {
        int ps = path.size();
        if (ps == depth)
            return replacement;
        if (ps < depth)
            throw new RuntimeException("path overflow");

        if (!(src instanceof Compound))
            return src; //path wont continue inside an atom

        int n = src.size();
        Compound csrc = (Compound) src;

        Term[] target = new Term[n];

        for (int i = 0; i < n; i++) {
            Term x = csrc.term(i);
            if (path.get(depth) != i)
                //unchanged subtree
                target[i] = x;
            else {
                //replacement is in this subtree
                target[i] = transform(x, path, depth + 1, replacement);
            }

        }

        return the(csrc.op(), csrc.dt(), target);
    }


    @NotNull
    public Term parseRaw(@NotNull String termToParse) throws Narsese.NarseseException {
        return Narsese.the().term(termToParse, this, false);
    }

    @Nullable
    public <T extends Termed> T parse(@NotNull String termToParse) throws Narsese.NarseseException {
        return (T) /*the*/(Narsese.the().term(termToParse, this, true));
    }

    /**
     * applies normalization and anonymization to resolve the term of the concept the input term maps t
     */
    @Nullable
    public final Concept concept(@NotNull Term term, boolean createIfMissing) {

        term = conceptualizable(term);
        if (term == null)
            return null;

        @Nullable Termed c = get(term, createIfMissing);
        if (!(c instanceof Concept)) {
            if (createIfMissing) {
                throw new InvalidConceptException(term, "Failed to build concept");
            }
            return null;
        }

        Concept cc = (Concept) c;
        if (cc.policy() == null) {
            conceptBuilder().init(cc);
        }

        return cc;
    }

    public Term conceptualizable(@NotNull Term term) {
        Term termPre;
        do {
            termPre = term;

            switch (term.op()) {
                case VAR_DEP:
                case VAR_INDEP:
                case VAR_QUERY:
                case VAR_PATTERN:
                    //throw new InvalidConceptException((Compound)term, "variables can not be conceptualized");
                    return null;
                case NEG:
                    term = $.unneg(term);
                    break;

                default:

                    if (term instanceof Compound) {

                        if (term.size() == 0)
                            return null; //example: ()

                        term = normalize((Compound) term);
                    }

                    if (term instanceof Compound) {
                        term = Terms.atemporalize((Compound) term);
                    }

                    break;

            }
        } while (termPre != term && term != null);

        return term;
    }


    @Nullable
    public Term replace(@NotNull Term src, Map<Term, Term> m) {
        return termOrNull(transform(src, new MapSubst(m)));
    }

    @Nullable
    public Term replace(@NotNull Term src, Term from, Term to) {
        return replace(src, Maps.mutable.of(from, to));
    }


    public void loadBuiltins() {
        for (Concept t : TermTransform.StaticBuiltins)
            set(t);
    }

    /**
     * implementations can override this to update the index when a concept's state changes, ex: to re-evaluate it's features
     */
    public void onPolicyChanged(Concept c) {
        /* nothing */
    }


    static boolean isDeletable(Concept c) {
        return c.get(Concept.Savior.class) == null;
    }

    /**
     * attempts to delete a concept
     */
    protected void delete(@NotNull Concept value, @NotNull NAR nar) {
        if (isDeletable(value))
            value.delete(nar);
    }


}