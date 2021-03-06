//package nars.term.index;
//
//import com.google.common.cache.Cache;
//import com.google.common.cache.CacheBuilder;
//import javassist.scopedpool.SoftValueHashMap;
//import nars.Op;
//import nars.term.Term;
//import nars.term.TermIndex;
//import nars.term.Termed;
//import nars.term.container.TermContainer;
//import nars.time.Clock;
//import org.jetbrains.annotations.NotNull;
//import org.jetbrains.annotations.Nullable;
//
//import java.util.concurrent.ExecutionException;
//import java.util.function.Consumer;
//
///** TermIndex implemented with GuavaCache with
// * optional WeakRef policy.
// * suitable for running indefnitely and obeying AIKR
// * principles
// *
// * NOT TESTED  to be replaced with a better cache impl
// * */
//public class GuavaIndex implements TermIndex {
//
//    @NotNull
//    final Cache<Term,Termed> data;
//    @NotNull
//    final SoftValueHashMap subterms;
//
//
//    public GuavaIndex() {
//        this(CacheBuilder.newBuilder());
//    }
//
//    public GuavaIndex(Clock reasonerClock, int expirationCycles) {
//        this(CacheBuilder.newBuilder()
//                //.maximumSize(capacity)
//
////                .expireAfterWrite(expirationCycles, TimeUnit.NANOSECONDS)
////                .expireAfterAccess(expirationCycles, TimeUnit.NANOSECONDS)
////                .ticker(new Ticker() {
////                    @Override public long read() {
////                        return reasonerClock.time();
////                    }
////                })
//
//                //.weakValues()
//                //.softValues()
//
//                //.recordStats()
////                .removalListener((e) -> {
////                    if (e.getCause()!= RemovalCause.REPLACED)
////                        System.err.println("guava remove: " + e + " : " + e.getCause() );
////                }));
//        );
//
//    }
//
//    public GuavaIndex(@NotNull CacheBuilder cb) {
//        this.data = cb.builder();
//        this.subterms = new SoftValueHashMap();
////        subterms = CacheBuilder.newBuilder()
////                //.maximumSize(capacity)
////                .softValues()
//////                .removalListener((e) -> {
//////                    if (e.getCause()!= RemovalCause.REPLACED)
//////                        System.err.println("guava remove: " + e + " : " + e.getCause() );
//////                })
////              .builder();
//    }
//
//    @Override
//    public int subtermsCount() {
//        return subterms.size();
//    }
//
//    @Override
//    public void forEach(@NotNull Consumer<? super Termed> c) {
//        data.asMap().forEach((k,v) -> c.accept(k));
//    }
//
////    /** gets an existing item or applies the builder to produce something to return */
////    @Override
////    public <K extends Term> Termed<K> apply(@NotNull K key)  {
////        try {
////            return data.get(key, () -> builder.apply(key));
////        } catch (ExecutionException e) {
////            throw new RuntimeException(e);
////        }
////    }
//
//
//
//    @Nullable
//    @Override
//    public Termed getIfPresent(@NotNull Termed t) {
//        return data.getIfPresent(t.term());
//    }
//
//
//    @Override
//    public void clear() {
//        data.invalidateAll();
//        data.cleanUp();
////        subterms.invalidateAll();
////        subterms.cleanUp();
//    }
//
////    @Override
////    public Object remove(Term key) {
////        data.invalidate(key);
////        return key; //?
////    }
//
//    @Override
//    public void putTerm(@NotNull Termed termed) {
//        data.put(termed.term(), termed);
//    }
//
//    @Override
//    public int size() {
//        return (int)data.size();
//    }
//
//    @NotNull
//    @Override
//    public Termed make(/*@NotNull*/ Op op, int relation, TermContainer subterms, int dt) {
//        return AbstractMapIndex.intern(op, relation, internSub(subterms));
//    }
//
//    @NotNull
//    @Override
//    public TermContainer internSub(TermContainer s) {
////        try {
////            //return subterms.get(s, () -> internSubterms(s.terms()));
////        } catch (ExecutionException e) {
////            throw new RuntimeException(e);
////        }
//        return (TermContainer) subterms.computeIfAbsent(s,
//                (ss) -> unifySubterms((TermContainer) ss));
//    }
//
//
//    @Override
//    public Termed the(@NotNull Term x) {
//
////        if (!AbstractMapIndex.isInternable(x)) {
////            return x;
////        }
//
//        try {
//            return data.get(x, () -> resolve(x));
//        } catch (ExecutionException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//}
