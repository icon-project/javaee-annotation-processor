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

import score.Context;
import score.DictDB;

import java.util.Map;
import java.util.function.Function;

public class ProxyDictDB<K, V> implements ProxyDB{
    protected final String id;
    protected final Class<V> valueClass;
    protected final DictDB<Object, V> dictDB;
    protected final Class<K> keyClass;
    protected final Function<K, String> keyEncoder;
    private Map<K, V> origin;
    private Map<K, V> update;

    public ProxyDictDB(String id, Class<V> valueClass) {
        this(id, valueClass, null, null);
    }

    public ProxyDictDB(String id, Class<V> valueClass, Class<K> keyClass) {
        this(id, valueClass, keyClass, null);
    }

    public ProxyDictDB(String id, Class<V> valueClass, Class<K> keyClass, Function<K, String> keyEncoder) {
        if (keyClass != null && !Codec.isSupportedKeyType(keyClass) && keyEncoder == null) {
            throw new IllegalArgumentException("keyEncoder function required");
        }
        dictDB = Context.newDictDB(id, valueClass);
        this.id = id;
        this.keyClass = keyClass;
        this.valueClass = valueClass;
        this.keyEncoder = keyEncoder;
    }

    private Map<K, V> getOrigin() {
        if (origin == null) {
            origin = new scorex.util.HashMap<>();
        }
        return origin;
    }

    private Map<K, V> getUpdate() {
        if (update == null) {
            update = new scorex.util.HashMap<>();
        }
        return update;
    }

    private V ensureGet(K key) {
        V v;
        Map<K, V> origin = getOrigin();
        if (isLoaded(key)) {
            v = origin.get(key);
        } else {
            v = dictDB.get(encodeKey(key));
            origin.put(key, v);
            printKeyValue("ensureGet", key, v);
        }
        return v;
    }

    protected Object encodeKey(K key) {
        return keyEncoder != null ? keyEncoder.apply(key) : key;
    }

    public void set(K key, V value) {
        printKeyValue("set", key, value);
        getUpdate().put(key, value);
    }

    public V get(K key) {
        Map<K, V> update = getUpdate();
        if (isModified(key)) {
            return update.get(key);
        } else {
            return ensureGet(key);
        }
    }

    public V getOrDefault(K key, V defaultValue) {
        V value = get(key);
        return value == null ? defaultValue : value;
    }

    public V remove(K key) {
        V old = get(key);
        if (old != null) {
            set(key, null);
        }
        return old;
    }

    public boolean containsKey(K key) {
        return getUpdate().get(key) != null || ensureGet(key) != null;
    }

    public boolean isModified(K key) {
        return getUpdate().containsKey(key);
    }

    public boolean isLoaded(K key) {
        return getOrigin().containsKey(key);
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public void close() {
        Map<K, V> origin = getOrigin();
        for (Map.Entry<K, V> entry : origin.entrySet()) {
            V value = entry.getValue();
            if (value instanceof PropertiesDB){
                ((PropertiesDB<?>) value).close();
            }
            printKeyValue("close origin", entry.getKey(), value);
        }
        Map<K, V> update = getUpdate();
        for (Map.Entry<K, V> entry : update.entrySet()) {
            V value = entry.getValue();
            if (value instanceof PropertiesDB){
                ((PropertiesDB<?>) value).close();
            }
            printKeyValue("close update", entry.getKey(), value);
        }
        this.origin = null;
        this.update = null;
        println("close");
    }

    @Override
    public void flush() {
        Map<K, V> origin = getOrigin();
        Map<K, V> update = getUpdate();
        for (Map.Entry<K, V> entry : update.entrySet()) {
            K key = entry.getKey();
            V value = entry.getValue();
            //in case of (value == null && !origin.containsKey(key)) can use below
            //{dictDB.get(key) != null; dictDB.set(encodeKey(key), value);}
            V old = origin.get(key);
            printKeyValue("flush "+ (((old instanceof PropertiesDB) || (value instanceof PropertiesDB)) ? "PropertiesDB" : ""),
                    key, value);
            if (value == null) {
                if (old instanceof PropertiesDB) {
                    ((PropertiesDB<?>) old).value(null);
                    ((PropertiesDB<?>) old).flush();
                }
                dictDB.set(encodeKey(key), null);
            } else {
                if (value instanceof PropertiesDB) {
                    ((PropertiesDB<?>) value).flush();
                    dictDB.set(encodeKey(key), value);
                }
                dictDB.set(encodeKey(key), value);
            }
        }
    }

    public String concatID(String sub) {
        return id + "|" + sub;
    }
}