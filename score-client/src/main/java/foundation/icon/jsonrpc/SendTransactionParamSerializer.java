package foundation.icon.jsonrpc;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import foundation.icon.jsonrpc.model.SendTransactionParam;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.StringJoiner;
import java.util.TreeSet;

public class SendTransactionParamSerializer {
    static final String PREFIX = "icx_sendTransaction.";

    static ObjectMapper iconMapper = new ObjectMapper();
    static {
        iconMapper.registerModule(new IconJsonModule());
    }
    static ObjectMapper mapper = new ObjectMapper();

    public static String serialize(SendTransactionParam sendTransactionParam) throws IOException {
        return serialize(sendTransactionParam, null);
    }

    public static String serialize(SendTransactionParam sendTransactionParam, Map<String, Object> buffer) throws IOException {
        String json = iconMapper.writeValueAsString(sendTransactionParam);
        Map<String, Object> params = mapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        if (buffer != null) {
            buffer.putAll(params);
        }
        return PREFIX + serializeMap(params);
    }

    public static String serializeObject(Object object) {
        if (object == null) {
            return "\\0";
        } else if (object instanceof Map) {
            // noinspection unchecked
            return "{" + serializeMap((Map<String, Object>) object) + "}";
        } else if (object instanceof Collection) {
            return "[" + serializeArray(object) + "]";
        } else if (object instanceof String) {
            return escape((String) object);
        } else {
            throw new RuntimeException(String.format("not supported class:%s", object.getClass().getName()));
        }
    }

    public static String serializeMap(Map<String, Object> map) {
        StringJoiner joiner = new StringJoiner(".");
        TreeSet<String> keys = new TreeSet<>(map.keySet());
        for (String key : keys) {
            joiner.add(key);
            joiner.add(serializeObject(map.get(key)));
        }
        return joiner.toString();
    }

    public static String serializeArray(Object arrayOrCollection) {
        StringJoiner joiner = new StringJoiner(".");
        if (arrayOrCollection instanceof Collection) {
            Collection<?> collection = ((Collection<?>) arrayOrCollection);
            for (Object element : collection) {
                joiner.add(serializeObject(element));
            }
        } else {
            throw new RuntimeException(String.format("not supported class:%s", arrayOrCollection.getClass().getName()));
        }
        return joiner.toString();
    }

    public static String escape(String string) {
        return string.replaceAll("([\\\\.{}\\[\\]])", "\\\\$1");
    }

}
