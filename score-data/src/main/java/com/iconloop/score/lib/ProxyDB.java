package com.iconloop.score.lib;

import score.Address;
import score.Context;

import java.math.BigInteger;

public interface ProxyDB {
    String id();
    void close();
    void flush();
    default void flushAndClose() {
        flush();
        close();
    }
    default void println(String ... msg) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(getClass().getName()).append(":").append(id()).append("]");
        for(String s : msg) {
            sb.append(" ").append(s);
        }
        Context.println(sb.toString());
    }
    default void printKeyValue(String prefix, Object key, Object value) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(getClass().getName()).append(":").append(id()).append("]");
        sb.append(prefix);
        sb.append(" ").append("key: ").append(Util.toString(key));
        sb.append(" ").append("value: ").append(Util.toString(value));
        Context.println(sb.toString());
    }

    static boolean isSupportedKeyType(Class<?> clazz) {
        for(Class<?> type : supportedKeyTypes){
            if (type.equals(clazz)){
                return true;
            }
        }
        return false;
    }

    Class<?>[] supportedKeyTypes = new Class<?>[]{
            String.class,
            byte[].class,
            Address.class,
            BigInteger.class,
            Byte.class,
            Short.class,
            Integer.class,
            Long.class,
            Character.class
    };
}
