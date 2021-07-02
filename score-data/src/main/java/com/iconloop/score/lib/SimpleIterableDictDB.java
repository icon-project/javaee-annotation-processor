/*
 * Copyright 2021 ICON Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.iconloop.score.lib;

import score.ArrayDB;
import score.Context;
import scorex.util.ArrayList;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SimpleIterableDictDB<K, V> extends ProxyDictDB<K, V> implements IterableDictDB<K, V> {
    protected final ArrayDB<K> keys;

    public SimpleIterableDictDB(String id, Class<K> keyClass, Class<V> valueClass) {
        super(id, valueClass);
        this.keys = Context.newArrayDB(id+"|keys", keyClass);
    }

    @Override
    public void set(K key, V value) {
        if (value == null) {
            remove(key);
        } else {
            if (!super.containsKey(key)) {
                keys.add(key);
            }
            super.set(key, value);
        }
    }

    @Override
    public V remove(K key) {
        V v = super.remove(key);
        if (v != null) {
            K last = keys.pop();
            if(!last.equals(key)){
                for (int i = 0; i < keys.size(); i++) {
                    if (keys.get(i).equals(key)) {
                        keys.set(i, last);
                        return v;
                    }
                }
            }
        }
        return v;
    }

    @Override
    public int size() {
        return keys.size();
    }

    @Override
    public Set<K> keySet() {
        List<K> keys = new ArrayList<>();
        for (int i = 0; i < this.keys.size(); i++) {
            keys.add(this.keys.get(i));
        }
        return Immutables.newSet(keys);
    }

    @Override
    public Collection<V> values() {
        List<V> values = new ArrayList<>();
        for (int i = 0; i < this.keys.size(); i++) {
            K key = this.keys.get(i);
            V v = get(key);
            values.add(v);
        }
        return Immutables.newList(values);
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        List<Map.Entry<K, V>> entries = new ArrayList<>();
        for (int i = 0; i < this.keys.size(); i++) {
            K key = this.keys.get(i);
            V v = get(key);
            entries.add(Immutables.entry(key, v));
        }
        return Immutables.newSet(entries);
    }
}
