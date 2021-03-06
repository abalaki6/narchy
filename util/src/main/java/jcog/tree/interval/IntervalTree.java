package jcog.tree.interval;

import jcog.list.FasterList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

public class IntervalTree<K extends Comparable<? super K>, V> {
	
	@Nullable
	public IntervalTreeNode<K, V> root;

	@NotNull
	public List<V> searchOverlapping(Between<K> range){
		List<V> c = new FasterList();
		if(root != null){
			root.getOverlap(range, c);
		}
		return c;
	}

	@Nullable
	public final V getEqual(K low, K high){
		return getEqual(new Between<>(low, high));
	}

	@Nullable
	public V getEqual(Between<K> range){
		if(root != null){
			return root.getEqual(range);
		}
		return null;
	}
	
	@NotNull
	public List<V> searchOverlapping(K low, K high){
		return searchOverlapping(new Between<>(low, high));
	}
	
	/**
	 * Returns a collection of values that wholly contain the range specified.
	 */
	@NotNull
	public List<V> searchContaining(Between<K> range){
		List<V> c = new FasterList();
		if(root != null){
			root.getContain(range, c);
		}
		return c;
	}
	@NotNull
	public void forEachContainedBy(Between<K> range, BiConsumer<Between<K>,V> each){
		if(root != null){
			root.forEachContainedBy(range, each);
		}
	}

	/**
	 * Returns a collection of values that wholly contain the range specified.
	 */
	@NotNull
	public List<V> searchContaining(K low, K high){
		return searchContaining(new Between<>(low, high));
	}

	public void forEachContainedBy(K low, K high, BiConsumer<Between<K>,V> each){
		forEachContainedBy(new Between<>(low, high), each);
	}
	
	/**
	 * Returns a collection of values that are wholly contained by the range specified.
	 */
	@NotNull
	public List<V> searchContainedBy(Between<K> range){
		List<V> c = new FasterList();
		if(root != null){
			root.searchContainedBy(range, c);
		}
		return c;
	}
	
	/**
	 * Returns a collection of values that are wholly contained by the range specified.
	 */
	@NotNull
	public List<V> searchContainedBy(K low, K high){
		return searchContainedBy(new Between<>(low, high));
	}
	
	public void removeOverlapping(Between<K> range){
		if(root != null){
			root = root.removeOverlapping(range);
		}
	}
	
	public void removeOverlapping(K low, K high){
		removeOverlapping(new Between<>(low, high));
	}
	
	/**
	 * Returns a collection of values that wholly contain the range specified.
	 */
	public void removeContaining(Between<K> range){
		if(root != null){
			root = root.removeContaining(range);
		}
	}
	
	/**
	 * Returns a collection of values that wholly contain the range specified.
	 */
	public void removeContaining(K low, K high){
		removeContaining(new Between<>(low, high));
	}
	
	/**
	 * Returns a collection of values that are wholly contained by the range specified.
	 */
	public void removeContainedBy(Between<K> range){
		if(root != null){
			root = root.removeContainedBy(range);
		}
	}
	
	/**
	 * Returns a collection of values that are wholly contained by the range specified.
	 */
	public void removeContainedBy(K low, K high){
		removeContainedBy(new Between<>(low, high));
	}
	
	public boolean isEmpty() {
		return root == null;
	}

	public void put(Between<K> key, V value) {
		root = (root == null) ? new IntervalTreeLeaf<>(key, value) : root.put(key, value);
	}
	
	public void put(K low, K high, V value) {
		put(new Between<>(low, high),value);
	}

	public void put(K at, V value) {
		//TODO: put(new At<>(low, high),value);
		put(at, at, value);
	}


	public int size() {
		return values().size();
	}

	@NotNull
	public Collection<V> values() {
		Collection<V> c = new FasterList();
		if(root != null){
			root.values(c);
		}
		return c;
	}

	@NotNull
	public Set<Between<K>> keySet() {
		if(root != null){
			Set<Between<K>> s = new HashSet(1); //ArrayUnenforcedSet?
			root.keySet(s);
			return s;
		} else {
			return Collections.emptySet();
		}
	}


	public SortedSet<Between<K>> keySetSorted() {
		if(root != null){
			TreeSet<Between<K>> s = new TreeSet();
			root.keySet(s);
			return s;
		} else {
			return Collections.emptySortedSet();
		}
	}

	public void putAll(Map<Between<K>, V> m) {
		for(Entry<Between<K>, V> intervalVEntry : m.entrySet()){
			put(intervalVEntry.getKey(), intervalVEntry.getValue());
		}
	}

	public void clear() {
		root = null;
	}

	public boolean containsValue(V value) {
		return root != null && root.containsValue(value);
	}

	@NotNull
	public Set<Entry<Between<K>, V>> entrySet() {
		Set<Entry<Between<K>, V>> s = new HashSet(size());
		if(root != null){
			root.entrySet(s);
		}
		return s;
	}

	public void remove(V value) {
		if(root != null){
			root = root.remove(value);
		}
	}
	
	public void removeAll(Collection<V> values) {
		if(root != null){
			root = root.removeAll(values);
		}
	}

	public final int height() {
		return root != null ? root.maxHeight() : 0;
	}

	public double averageHeight() {
		if(root == null){
			return 0.0;
		}

		//TODO use IntArrayList
		Collection<Integer> c = new LinkedList<>();
		root.averageHeight(c, 0);
		int count = 0;
		int total = 0;
		for(int i : c){
			total += i;
			count ++;
		}
		return (double) total / count;
		
	}


}
