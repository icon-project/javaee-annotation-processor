package com.iconloop.score.example.model;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import score.ObjectReader;
import score.ObjectWriter;

public class CustomConverter {
    public static Struct parseStruct(JsonValue jsonValue) {
        if (jsonValue == null || jsonValue.isNull()) {
            return null;
        }
        JsonObject jsonObject = jsonValue.asObject();
        Struct obj = new Struct();
        JsonValue valueJsonValue = jsonObject.get("value");
        if (valueJsonValue != null) {
            obj.setValue(valueJsonValue.asString());
        }
        return obj;
    }

    public static JsonValue toJson(Struct obj) {
        JsonObject jsonObject = Json.object();
        String value = obj.getValue();
        JsonValue valueJsonValue = Json.value(value);
        jsonObject.add("value", valueJsonValue);
        return jsonObject;
    }

    public static Struct readStruct(ObjectReader reader) {
        Struct obj = new Struct();
        obj.setValue(reader.readNullable(String.class));
        return obj;
    }

    public static void writeObject(ObjectWriter writer, Struct obj) {
        writer.writeNullable(obj.getValue());
    }
}
