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

import score.ObjectReader;
import score.ObjectWriter;
import scorex.util.ArrayList;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class LinkedIterableDictDB<K, V> extends ProxyDictDB<K, V> implements IterableDictDB<K, V> {

    protected final Codec<K, String> codec;
    @SuppressWarnings("rawtypes")
    protected final ProxyVarDB<Info> varDB;
    @SuppressWarnings("rawtypes")
    protected final ProxyDictDB<K, Link> proxyDictDB;

    public LinkedIterableDictDB(String id, Class<V> valueClass, Class<K> keyClass) {
        this(id, valueClass, keyClass, null, null);
    }

    public LinkedIterableDictDB(String id, Class<V> valueClass, Class<K> keyClass, Function<K, String> keyEncoder) {
        this(id, valueClass, keyClass, keyEncoder, null);
    }

    public LinkedIterableDictDB(String id, Class<V> valueClass, Class<K> keyClass, Function<K, String> keyEncoder, Function<String, K> keyDecoder) {
        super(id, valueClass, keyClass, keyEncoder);
        if (keyClass == null) {
            throw new IllegalArgumentException("keyClass cannot be null");
        }
        if(!ProxyDB.isSupportedKeyType(keyClass) && keyDecoder == null) {
            throw new IllegalArgumentException("keyDecoder function required");
        }
        if (keyEncoder != null && keyDecoder != null) {
            codec = new Codec<>(keyEncoder, keyDecoder);
        } else {
            codec = StringCodec.resolve(keyClass);
            if (codec == null) {
                throw new IllegalArgumentException("keyEncoder, keyDecoder function required for keyClass:"+keyClass);
            }
        }
        varDB = new ProxyVarDB<>(concatID(id, "info"), Info.class);
        proxyDictDB = new ProxyDictDB<>(concatID(id, "nodes"), Link.class, keyClass);
    }

    @SuppressWarnings("unchecked")
    protected Info<K> getInfo() {
        Info<K> info = varDB.get();
        println("getInfo info:", info == null ? "null" : info.toString());
        if (info == null) {
            info = new Info<>();
            info.setCodec(codec);
            setInfo(info);
        } else {
            if (info.getCodec() == null) {
                info.setCodec(codec);
                info.decode();
            }
        }
        return info;
    }

    protected void setInfo(Info<K> info) {
        varDB.set(info);
        println("setInfo info:", info == null ? "null" : info.toString());
    }

    @Override
    public void set(K key, V value) {
        println("set key:", key.toString());
        if (value == null) {
            remove(key);
        } else {
            if (!proxyDictDB.containsKey(key)) {
                putLast(key, value);
            } else {
                super.set(key, value);
            }
        }
    }

    private Link<K> newNode(K key) {
        Link<K> link = new Link<>();
        link.setCodec(codec);
        println("newNode key:",key.toString());
        return link;
    }

    @SuppressWarnings("unchecked")
    private Link<K> getNode(K key) {
        Link<K> link = proxyDictDB.get(key);
        if (link != null) {
            link.setCodec(codec);
            link.decode();
        }
        return link;
    }

    private void setNode(K key, Link<K> link) {
        if (link != null) {
            link.encode();
        }
        proxyDictDB.set(key, link);
    }

    @SuppressWarnings("unchecked")
    private void detach(Link<K> link) {
        if (link.hasNext()) {
            Link<K> next = getNode(link.getNext());
            next.setPrev(link.getPrev());
            setNode(link.getNext(), next);
            link.setNext(null);
        }
        if (link.hasPrev()) {
            Link<K> prev = getNode(link.getPrev());
            prev.setNext(link.getNext());
            setNode(link.getPrev(), prev);
            link.setPrev(null);
        }
    }

    @SuppressWarnings("unchecked")
    public void putLast(K key, V value) {
        if (value == null) {
            throw new IllegalArgumentException("value cannot be null");
        }
        Info<K> info = getInfo();
        if (!info.isLast(key)) {
            if (!info.isEmpty()) {
                Link<K> last = getNode(info.getLast());
                last.setNext(key);
                setNode(info.getLast(), last);
            }

            Link<K> link = getNode(key);
            if (link == null) {
                link = newNode(key);
                if (info.isEmpty()) {
                    info.setFirst(key);
                }
                info.addSize(1);
            } else {
                detach(link);
            }
            link.setPrev(info.getLast());
            setNode(key, link);

            info.setLast(key);
            setInfo(info);
        }
        super.set(key, value);
    }

    @SuppressWarnings("unchecked")
    public void putFirst(K key, V value) {
        if (value == null) {
            throw new IllegalArgumentException("value cannot be null");
        }
        Info<K> info = getInfo();
        if (!info.isFirst(key)) {
            if (!info.isEmpty()) {
                Link<K> first = getNode(info.getFirst());
                first.setPrev(key);
                setNode(info.getFirst(), first);
            }

            Link<K> link = getNode(key);
            if (link == null) {
                link = newNode(key);
                if (info.isEmpty()) {
                    info.setLast(key);
                }
                info.addSize(1);
            } else {
                detach(link);
            }
            link.setNext(info.getFirst());
            setNode(key, link);

            info.setFirst(key);
            setInfo(info);
        }
        super.set(key, value);
    }

    @Override
    public V remove(K key) {
        V v = super.remove(key);
        if (v != null) {
            Link<K> link = getNode(key);
            Info<K> info = getInfo();
            if (info.isFirst(key)) {
                info.setFirst(link.getNext());
            }
            if (info.isLast(key)) {
                info.setLast(link.getPrev());
            }
            info.addSize(-1);

            detach(link);
            setNode(key, null);
            setInfo(info);
        }
        return v;
    }

    @Override
    public int size() {
        return getInfo().getSize();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<K> keySet() {
        List<K> keys = new ArrayList<>();
        for (K key = getInfo().getFirst(); key != null; key = getNode(key).getNext()) {
            keys.add(key);
        }
        return Immutables.newSet(keys);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<V> values() {
        List<V> values = new ArrayList<>();
        for (K key = getInfo().getFirst(); key != null; key = getNode(key).getNext()) {
            V v = get(key);
            values.add(v);
        }
        return Immutables.newList(values);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        List<Map.Entry<K, V>> entries = new ArrayList<>();
        for (K key = getInfo().getFirst(); key != null; key = getNode(key).getNext()) {
            V v = get(key);
            println("entries key:", key.toString(), "value:", v == null ? "null" : "obj");
            entries.add(Immutables.entry(key, v));
        }
        return Immutables.newSet(entries);
    }

    @Override
    public void close() {
        super.close();
        varDB.close();
        proxyDictDB.close();
    }

    @Override
    public void flush() {
        super.flush();
        varDB.flush();
        proxyDictDB.flush();
    }

    public static class Info<K> {
        String rawFirst;
        String rawLast;
        K first;
        K last;
        int size;
        Codec<K, String> codec;

        public Codec<K, String> getCodec() {
            return codec;
        }

        public void setCodec(Codec<K, String> codec) {
            this.codec = codec;
        }

        private void requireCodec() throws IllegalStateException {
            if (codec == null) {
                throw new IllegalStateException("Info codec is null");
            }
        }

        public void encode() {
            requireCodec();
            rawFirst = codec.encode(first);
            rawLast = codec.encode(last);
        }

        public void decode() {
            requireCodec();
            first = codec.decode(rawFirst);
            last = codec.decode(rawLast);
        }

        public K getFirst() {
            requireCodec();
            return first;
        }

        public void setFirst(K first) {
            this.first = first;
        }

        public boolean isFirst(K key) {
            requireCodec();
            return first != null && first.equals(key);
        }

        public K getLast() {
            requireCodec();
            return last;
        }

        public void setLast(K last) {
            this.last = last;
        }

        public boolean isLast(K key) {
            requireCodec();
            return last != null && last.equals(key);
        }

        public boolean isEmpty() {
            return size == 0;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public void addSize(int size) {
            this.size += size;
        }

        @SuppressWarnings("unused")
        public static void writeObject(ObjectWriter w, Info<?> obj) {
            obj.encode();
            w.writeNullable(obj.rawFirst);
            w.writeNullable(obj.rawLast);
            w.write(obj.size);
        }

        @SuppressWarnings("unused")
        public static Info<?> readObject(ObjectReader r) {
            Info<?> obj = new Info<>();
            obj.rawFirst = r.readNullable(String.class);
            obj.rawLast = r.readNullable(String.class);
            obj.size = r.readInt();
            return obj;
        }

        @Override
        public String toString() {
            return "Info{" +
                    "rawFirst='" + rawFirst + '\'' +
                    ", rawLast='" + rawLast + '\'' +
                    ", first=" + first +
                    ", last=" + last +
                    ", size=" + size +
                    ", codec=" + codec +
                    '}';
        }
    }

    public static class Link<K> {
        String rawPrev;
        String rawNext;
        K prev;
        K next;
        Codec<K, String> codec;

        public Codec<K, String> getCodec() {
            return codec;
        }

        public void setCodec(Codec<K, String> codec) {
            this.codec = codec;
        }

        private void requireCodec() throws IllegalStateException {
            if (codec == null) {
                throw new IllegalStateException("Link codec is null");
            }
        }

        public void encode() {
            requireCodec();
            rawPrev = codec.encode(prev);
            rawNext = codec.encode(next);
        }

        public void decode() {
            requireCodec();
            prev = codec.decode(rawPrev);
            next = codec.decode(rawNext);
        }

        public K getPrev() {
            requireCodec();
            return prev;
        }

        public void setPrev(K prev) {
            this.prev = prev;
        }

        public boolean hasPrev() {
            requireCodec();
            return prev != null;
        }

        public K getNext() {
            requireCodec();
            return next;
        }

        public void setNext(K next) {
            this.next = next;
        }

        public boolean hasNext() {
            requireCodec();
            return next != null;
        }

        @SuppressWarnings("unused")
        public static void writeObject(ObjectWriter w, Link<?> obj) {
            w.writeNullable(obj.rawPrev);
            w.writeNullable(obj.rawNext);
        }

        @SuppressWarnings("unused")
        public static Link<?> readObject(ObjectReader r) {
            Link<?> obj = new Link<>();
            obj.rawPrev = r.readNullable(String.class);
            obj.rawNext = r.readNullable(String.class);
            return obj;
        }

        @Override
        public String toString() {
            return "Link{" +
                    "rawPrev='" + rawPrev + '\'' +
                    ", rawNext='" + rawNext + '\'' +
                    ", prev=" + prev +
                    ", next=" + next +
                    ", codec=" + codec +
                    '}';
        }
    }

}
