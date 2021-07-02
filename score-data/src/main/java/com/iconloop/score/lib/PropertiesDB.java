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

import java.util.Map;

/**
 * To implement PropertiesDB interface, must declare below static functions
 * <pre>
 * {@code
 * public static T readObject(ObjectReader) { return new T();}
 * public static void writeObject(ObjectWriter, T) {}
 * }
 * </pre>
 *
 * @param <T> POJO class
 */
public interface PropertiesDB<T> extends ProxyDB {
    /**
     * initialize PropertiesDB
     *
     * @param id key for DB
     */
    void initialize(String id);

    /**
     * Overwrite as given object
     *
     * @param obj the object to be copied
     */
    void value(T obj);

    /**
     * Returns POJO object instead of PropertiesDB instance
     *
     * @return POJO object
     */
    T value();

    /**
     * Returns properties as {@link java.util.Map}
     *
     * @return properties as {@link java.util.Map}
     */
    Map<String, Object> toMap();

    /**
     * Check initialized
     *
     * @param db internal ProxyDictDB
     * @throws java.lang.IllegalStateException if not initialized
     */
    default void requireInitialized(ProxyDictDB<?,?> db) {
        if (db == null) {
            throw new IllegalStateException("db is not initialized");
        }
    }

    /**
     * Check not initialized
     *
     * @param db internal ProxyDictDB
     * @throws java.lang.IllegalStateException if initialized
     */
    default void requireNotInitialized(ProxyDictDB<?,?> db) {
        if (db != null) {
            throw new IllegalStateException("db is already initialized");
        }
    }

    static String readObject(ObjectReader reader) {
        return reader.readString();
    }

    static void writeObject(ObjectWriter writer, PropertiesDB<?> obj) {
        writer.write(obj.id());
    }
}
