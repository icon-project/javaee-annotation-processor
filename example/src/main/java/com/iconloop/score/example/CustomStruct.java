package com.iconloop.score.example;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import score.ObjectReader;
import score.ObjectWriter;

public class CustomStruct {
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public static CustomStruct customParse(JsonObject jsonObject) {
        CustomStruct obj = new CustomStruct();
        JsonValue valueJsonValue = jsonObject.get("value");
        if (valueJsonValue != null) {
            obj.setValue(valueJsonValue.asString());
        }
        return obj;
    }

    public static JsonObject customToJson(CustomStruct obj) {
        JsonObject jsonObject = Json.object();
        String value = obj.getValue();
        JsonValue valueJsonValue = Json.value(value);
        jsonObject.add("value", valueJsonValue);
        return jsonObject;
    }

    public static CustomStruct customReadObject(ObjectReader reader) {
        CustomStruct obj = new CustomStruct();
        obj.setValue(reader.readString());
        return obj;
    }

    public static void customWriteObject(ObjectWriter writer, CustomStruct obj) {
        writer.writeNullable(obj.getValue());
    }
}
