package com.iconloop.score.lib;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class Immutables {
    /**
     * No instances.
     */
    private Immutables() {
    }

    public static UnsupportedOperationException uoe() {
        return new UnsupportedOperationException();
    }

    /**
     * Returns an unmodifiable list containing an arbitrary number of elements.
     * Usable instead of {@link java.util.List#of(E... elements)}
     *
     * @param list List
     * @param <E> the type of elements
     * @return a {@link java.util.List} containing the specified mappings
     * return null if the list is null.
     */
    public static <E> List<E> newList(List<E> list) {
        if (list != null) {
            return new ImmutableList<>(list);
        }
        return null;
    }

    /**
     * Returns an unmodifiable set containing an arbitrary number of elements.
     *
     * @param coll Collection
     * @param <E> the type of elements
     * @return a {@link java.util.Set} containing the specified elements
     * return null if the col is null.
     */
    public static <E> Set<E> newSet(Collection<E> coll) {
        if (coll != null) {
            return new ImmutableSet<>(coll);
        }
        return null;
    }

    /**
     * Returns an unmodifiable {@link Map.Entry} containing the given key and value.
     * Usable instead of {@link java.util.Map#entry}
     *
     * @param <K>   the key's type
     * @param <V>   the value's type
     * @param key   the key
     * @param value the value
     * @return an {@link java.util.Map.Entry} containing the specified key and value,
     * return null if the key is null.
     */
    public static <K, V> Map.Entry<K, V> entry(K key, V value) {
        if (key != null) {
            return new ImmutableEntry<>(key, value);
        }
        return null;
    }

    /**
     * Returns an unmodifiable map containing keys and values extracted from the given entries.
     * Usable instead of {@link java.util.Map#ofEntries}
     *
     * @param <K>   the key's type
     * @param <V>   the value's type
     * @param entries {@link java.util.Map.Entry}s containing the keys and values from which the map is populated
     * @return a {@link java.util.Map} containing the specified mappings
     * return null if the entries is null.
     */
    public static <K, V> Map<K, V> ofEntries(Collection<Map.Entry<K, V>> entries) {
        if (entries != null) {
            Map<K, V> map = new scorex.util.HashMap<>();
            for (Map.Entry<K, V> entry : entries) {
                map.put(entry.getKey(), entry.getValue());
            }
            return new ImmutableMap<>(map);
        }
        return null;
    }

    public static class ImmutableCollection<E> implements Collection<E> {
        Collection<E> coll;

        private ImmutableCollection(Collection<E> coll) {
            this.coll = coll;
        }

        @Override
        public int size() {
            return coll.size();
        }

        @Override
        public boolean isEmpty() {
            return coll.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return coll.contains(o);
        }

        @Override
        public Iterator<E> iterator() {
            return coll.iterator();
        }

        @Override
        public Object[] toArray() {
            return coll.toArray();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return coll.toArray(a);
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return coll.containsAll(c);
        }

        // all mutating methods throw UnsupportedOperationException
        @Override
        public boolean add(E e) {
            throw uoe();
        }

        @Override
        public boolean addAll(Collection<? extends E> c) {
            throw uoe();
        }

        @Override
        public void clear() {
            throw uoe();
        }

        @Override
        public boolean remove(Object o) {
            throw uoe();
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            throw uoe();
        }

        @Override
        public boolean removeIf(Predicate<? super E> filter) {
            throw uoe();
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            throw uoe();
        }
    }

    public static class ImmutableList<E> extends ImmutableCollection<E> implements List<E> {
        List<E> list;

        private ImmutableList(List<E> list) {
            super(list);
            this.list = list;
        }

        @Override
        public void add(int index, E element) {
            throw uoe();
        }

        @Override
        public boolean addAll(int index, Collection<? extends E> c) {
            throw uoe();
        }

        @Override
        public E remove(int index) {
            throw uoe();
        }

        @Override
        public void replaceAll(UnaryOperator<E> operator) {
            throw uoe();
        }

        @Override
        public E set(int index, E element) {
            throw uoe();
        }

        @Override
        public void sort(Comparator<? super E> c) {
            throw uoe();
        }

        //implements java.util.List
        @Override
        public int indexOf(Object o) {
            return list.indexOf(o);
        }

        @Override
        public int lastIndexOf(Object o) {
            return list.lastIndexOf(o);
        }

        @Override
        public ListIterator<E> listIterator() {
            return list.listIterator();
        }

        @Override
        public ListIterator<E> listIterator(int index) {
            return list.listIterator(index);
        }

        @Override
        public List<E> subList(int fromIndex, int toIndex) {
            return list.subList(fromIndex, toIndex);
        }

        @Override
        public E get(int index) {
            return list.get(index);
        }

        @Override
        public int size() {
            return list.size();
        }

        @Override
        public boolean isEmpty() {
            return list.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return list.contains(o);
        }

        @Override
        public Iterator<E> iterator() {
            return list.iterator();
        }

        @Override
        public Object[] toArray() {
            return list.toArray();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return list.toArray(a);
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return list.containsAll(c);
        }
    }

    public static class ImmutableSet<E> extends ImmutableCollection<E> implements Set<E> {
        private ImmutableSet(Collection<E> coll) {
            super(coll);
        }
    }

    public static class ImmutableEntry<K, V> implements Map.Entry<K, V> {
        private final K key;
        private final V value;

        private ImmutableEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public final K getKey() {
            return key;
        }

        public final V getValue() {
            return value;
        }

        public final String toString() {
            return key + "=" + value;
        }

        public final int hashCode() {
            return (key == null ? 0 : key.hashCode()) ^
                    (value == null ? 0 : value.hashCode());
        }

        public final V setValue(V newValue) {
            throw uoe();
        }

        public final boolean equals(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
            return Util.eq(key, e.getKey()) && Util.eq(value, e.getValue());
        }
    }

    public static class ImmutableMap<K, V> implements Map<K, V> {
        final Map<K, V> map;
        final Set<Entry<K, V>> entries;
        final Set<K> keySet;
        final Collection<V> values;

        private ImmutableMap(Map<K, V> map) {
            this.map = map;
            entries = new ImmutableSet<>(map.entrySet());
            keySet = new ImmutableSet<>(map.keySet());
            values = new ImmutableCollection<>(map.values());
        }

        @Override
        public void clear() {
            throw uoe();
        }

        @Override
        public Set<K> keySet() {
            return keySet;
        }

        @Override
        public Collection<V> values() {
            return values;
        }

        @Override
        public Set<Entry<K, V>> entrySet() {
            return entries;
        }

        @Override
        public V compute(K key, BiFunction<? super K, ? super V, ? extends V> rf) {
            throw uoe();
        }

        @Override
        public V computeIfAbsent(K key, Function<? super K, ? extends V> mf) {
            throw uoe();
        }

        @Override
        public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> rf) {
            throw uoe();
        }

        @Override
        public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> rf) {
            throw uoe();
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean containsKey(Object key) {
            return false;
        }

        @Override
        public boolean containsValue(Object value) {
            return false;
        }

        @Override
        public V get(Object key) {
            return null;
        }

        @Override
        public V put(K key, V value) {
            throw uoe();
        }

        @Override
        public void putAll(Map<? extends K, ? extends V> m) {
            throw uoe();
        }

        @Override
        public V putIfAbsent(K key, V value) {
            throw uoe();
        }

        @Override
        public V remove(Object key) {
            throw uoe();
        }

        @Override
        public boolean remove(Object key, Object value) {
            throw uoe();
        }

        @Override
        public V replace(K key, V value) {
            throw uoe();
        }

        @Override
        public boolean replace(K key, V oldValue, V newValue) {
            throw uoe();
        }

        @Override
        public void replaceAll(BiFunction<? super K, ? super V, ? extends V> f) {
            throw uoe();
        }

    }


}
