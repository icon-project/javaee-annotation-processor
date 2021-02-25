package com.iconloop.score.example;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

public class CustomPojo {
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public static CustomPojo parse(String jsonString) {
        return parse(Json.parse(jsonString).asObject());
    }

    public static JsonObject toJsonObject(CustomPojo obj) {
        return obj.toJsonObject();
    }

    public static CustomPojo parse(JsonObject jsonObject) {
        CustomPojo obj = new CustomPojo();
        JsonValue valueJsonValue = jsonObject.get("value");
        if (valueJsonValue != null) {
            obj.setValue(valueJsonValue.asString());
        }
        return obj;
    }

    public JsonObject toJsonObject() {
        JsonObject jsonObject = Json.object();
        String value = this.getValue();
        JsonValue valueJsonValue = Json.value(value);
        jsonObject.add("value", valueJsonValue);
        return jsonObject;
    }
}
