package com.iconloop.score.example;

import score.ArrayDB;
import score.Context;
import score.DictDB;

import java.util.Map;

public class EnumerableDictDB<K, V> {
    private final DictDB<K, Integer> indexes;
    private final DictDB<Integer, K> keys;
    private final ArrayDB<V> values;

    public EnumerableDictDB(String id, Class<K> keyClass, Class<V> valueClass) {
        // key => array index
        this.indexes = Context.newDictDB(id, Integer.class);
        // array index => key
        this.keys = Context.newDictDB(id+"|keys", keyClass);
        // array of valueClass
        this.values = Context.newArrayDB(id, valueClass);
    }

    public int size() {
        return values.size();
    }

    public boolean contains(K key) {
        return indexes.get(key) != null;
    }

    public V get(int i) {
        Context.require(i < values.size());
        return values.get(i);
    }

    public V get(K key) {
        Integer i = indexes.get(key);
        return (i != null) ? values.get(i) : null;
    }

    public V put(K key, V value) {
        Integer i = indexes.get(key);
        if (i != null) {
            V old = values.get(i);
            values.set(i, value);
            return old;
        } else {
            i = values.size();
            indexes.set(key, i);
            keys.set(i, key);
            values.add(value);
            return null;
        }
    }

    public V remove(K key) {
        Integer i = indexes.get(key);
        if (i != null) {
            V old = values.get(i);
            V last = values.pop();
            Integer lastIdx = values.size();
            indexes.set(key, null);
            if (i.equals(lastIdx)) {
                keys.set(lastIdx, null);
            } else {
                K lastKey = keys.get(lastIdx);
                indexes.set(lastKey, i);
                keys.set(i, lastKey);
                values.set(i, last);
            }
            return old;
        }
        return null;
    }

    public Map<K, V> toMap() {
        int size = size();
        Map.Entry[] entries = new Map.Entry[size];
        for(int i=0; i < size; i++) {
            entries[i] = Map.entry(keys.get(i), values.get(i));
        }
        return Map.ofEntries(entries);
    }
}
