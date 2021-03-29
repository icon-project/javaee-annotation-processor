package com.iconloop.score.example.model;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import score.ObjectReader;
import score.ObjectWriter;

public class Struct {
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    //ScoreDataObjectProcessor auto-detect readObject and writeObject functions without fixed name
    public static Struct readObject(ObjectReader reader) {
        Struct obj = new Struct();
        obj.setValue(reader.readNullable(String.class));
        return obj;
    }

    public static void writeObject(ObjectWriter writer, Struct obj) {
        writer.writeNullable(obj.getValue());
    }

    //JsonObjectProcessor auto-detect parse and toJson functions without fixed name
    public static Struct parse(JsonValue jsonValue) {
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
}
