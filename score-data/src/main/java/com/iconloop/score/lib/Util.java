package com.iconloop.score.lib;

import score.Address;
import score.Context;
import scorex.util.ArrayList;
import scorex.util.HashMap;
import scorex.util.StringTokenizer;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Util {
    public static boolean eq(Object o1, Object o2) {
        return o1 == null ? o2 == null : o1.equals(o2);
    }

    public static <T> T requireNonNullElse(T obj, T defaultObj) {
        return (obj != null) ? obj : requireNonNull(defaultObj, "defaultObj");
    }

    public static <T> T requireNonNull(T obj, String message) {
        if (obj == null)
            throw new NullPointerException(message);
        return obj;
    }

    public static String toLowerCamelCase(String str) {
        String camelCase = toCamelCase(str);
        if (camelCase.length() > 0) {
            StringBuilder builder = new StringBuilder();
            builder.append(camelCase.substring(0, 1).toLowerCase());
            if (camelCase.length() > 1) {
                builder.append(camelCase.substring(1));
            }
        }
        return camelCase;
    }

    public static String toCamelCase(String str) {
        StringBuilder builder = new StringBuilder();
        List<String> words = tokenize(str, '_');
        for (String word : words) {
            if (word.length() > 0) {
                String lower = word.toLowerCase();
                builder.append(lower.substring(0, 1).toUpperCase());
                if (word.length() > 1) {
                    builder.append(lower.substring(1));
                }
            }
        }
        return builder.toString();
    }

    public static List<String> tokenize(String str, char... delimiters) {
        List<String> list = new ArrayList<>();
        StringTokenizer st = new StringTokenizer(str, new String(delimiters));
        while (st.hasMoreTokens()) {
            list.add(st.nextToken());
        }
        return list;
    }

    public static String[] toStringArray(List<String> list) {
        String[] arr = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            arr[i] = list.get(i);
        }
        return arr;
    }

    public static Address[] toAddressArray(List<Address> list) {
        Address[] arr = new Address[list.size()];
        for (int i = 0; i < list.size(); i++) {
            arr[i] = list.get(i);
        }
        return arr;
    }

    private static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hexChars[i * 2] = HEX_ARRAY[v >>> 4];
            hexChars[i * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] hexToBytes(String hexString) {
        if (hexString.length() % 2 > 0) {
            throw new IllegalArgumentException("hex cannot has odd length");
        }
        int l = hexString.length()/2;
        byte[] bytes = new byte[l];
        for (int i = 0; i < l; i++) {
            bytes[i] = (byte)((Character.digit(hexString.charAt(i), 16) << 4) |
                    Character.digit(hexString.charAt(i+1), 16));
        }
        return bytes;
    }

    static String loggerId(Class<?> clazz) {
        return clazz.getName();
    }
    static Map<String, Logger> loggers = new HashMap<>();

    public static Logger getLogger(Class<?> clazz) {
        String id = loggerId(clazz);
        Logger logger = loggers.get(id);
        if(logger == null) {
            logger = new Logger(id);
            loggers.put(id, logger);
        }
        return logger;
    }

    public static class Logger {
        public static final String DELIMITER = " ";
        final String id;

        public Logger(String id) {
            this.id = id;
        }

        public void println(String ... msg) {
            StringBuilder sb = new StringBuilder();
            sb.append("[").append(this.id).append("]");
            for(String s : msg) {
                sb.append(DELIMITER).append(s);
            }
            Context.println(sb.toString());
        }

        public void println(String prefix, Object ... objs) {
            StringBuilder sb = new StringBuilder();
            sb.append("[").append(this.id);
            if (prefix != null) {
                sb.append(":").append(prefix);
            }
            sb.append("]");
            for(Object obj : objs) {
                sb.append(DELIMITER).append(Util.toString(obj));
            }
            Context.println(sb.toString());
        }

        public void printKeyValue(String prefix, Object key, Object value) {
            StringBuilder sb = new StringBuilder();
            sb.append("[").append(this.id).append(":").append(prefix).append("]");
            sb.append(DELIMITER).append("key: ").append(toString(key));
            sb.append(DELIMITER).append("value: ").append(toString(value));
            Context.println(sb.toString());
        }

        public static String toString(Object obj) {
            if (obj == null) {
                return "null";
            } else {
                if (obj instanceof String) {
                    return (String)obj;
                } else {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Class<").append(obj.getClass().getName()).append(">");
                    sb.append(obj.toString());
                    return sb.toString();
                }
            }
        }
    }
    public static String toString(Object obj) {
        if (obj == null) {
            return "null";
        } else {
            return obj.toString();
        }
    }
    public static String toString(Object[] arr) {
        if (arr == null) {
            return "null";
        } else {
            StringBuilder sb = new StringBuilder("[");
            if (arr.length > 1) {
                sb.append(arr[0]);
            }
            for(int i=1;i < arr.length;i++) {
                sb.append(",").append(toString(arr[i]));
            }
            return sb.append("]").toString();
        }
    }
}
