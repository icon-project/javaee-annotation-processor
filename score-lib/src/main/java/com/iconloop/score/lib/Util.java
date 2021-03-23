package com.iconloop.score.lib;

import score.Context;

public class Util {
    public static boolean eq(Object o1, Object o2) {
        return o1 == null ? o2 == null : o1.equals(o2);
    }

    public static Logger getLogger(Class<?> clazz) {
        return new Logger(clazz);
    }
    public static class Logger {
        final String prefix;

        public Logger(Class<?> clazz) {
            prefix = "["+clazz.getName()+"]";
        }

        public void println(String ... msg) {
            StringBuilder sb = new StringBuilder();
            sb.append(prefix);
            for(String s : msg) {
                sb.append(" ").append(s);
            }
            Context.println(sb.toString());
        }
    }

    public static String toString(Object obj) {
        if (obj == null) {
            return "null";
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("Class<").append(obj.getClass().getName()).append(">");
            sb.append(obj.toString());
            return sb.toString();
        }
    }
}
