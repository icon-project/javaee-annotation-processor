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

import score.Address;

import java.math.BigInteger;
import java.util.function.Function;

public class Codec<D, E> {

    Function<D, E> encode;
    Function<E, D> decode;

    public Codec(Function<D, E> encode, Function<E, D> decode) {
        if (encode == null) {
            throw new IllegalArgumentException("encode cannot be null");
        }
        if (decode == null) {
            throw new IllegalArgumentException("decode cannot be null");
        }
        this.encode = encode;
        this.decode = decode;
    }

    public E encode(D d) {
        return d == null ? null : encode.apply(d);
    }

    public D decode(E e) {
        return e == null ? null : decode.apply(e);
    }

    public static void checkSupportedType(Object obj) {
        if (!((obj instanceof String) ||
                (obj instanceof byte[]) ||
                (obj instanceof Address) ||
                (obj instanceof BigInteger) ||
                (obj instanceof Byte) ||
                (obj instanceof Short) ||
                (obj instanceof Integer) ||
                (obj instanceof Long) ||
                (obj instanceof Character) ||
                (obj instanceof Boolean))) {
            throw new IllegalArgumentException("not supported");
        }
    }

    public static boolean isDefaultSupportedType(Class<?> clazz) {
        for(Class<?> type : defaultSupportedTypes){
            if (type.equals(clazz)){
                return true;
            }
        }
        return false;
    }

    public static final Class<?>[] defaultSupportedTypes = new Class<?>[]{
            String.class,
            byte[].class,
            Address.class,
            BigInteger.class,
            Byte.class,
            Short.class,
            Integer.class,
            Long.class,
            Character.class,
            Boolean.class
    };
}
