package com.iconloop.score.lib;

import score.Context;

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
}
