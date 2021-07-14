package foundation.icon.score.example.model;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import score.ByteArrayObjectWriter;
import score.Context;
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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Struct{");
        sb.append("value='").append(value).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Struct struct = (Struct) o;

        return value != null ? value.equals(struct.value) : struct.value == null;
    }

    public static void writeObject(ObjectWriter writer, Struct obj) {
        obj.writeObject(writer);
    }

    //ScoreDataObjectProcessor auto-detect readObject and writeObject functions without fixed name
    public static Struct readObject(ObjectReader reader) {
        Struct obj = new Struct();
        reader.beginList();
        obj.setValue(reader.readNullable(String.class));
        reader.end();
        return obj;
    }

    public void writeObject(ObjectWriter writer) {
        writer.beginList(1);
        writer.writeNullable(this.getValue());
        writer.end();
    }

    public static Struct fromBytes(byte[] bytes) {
        ObjectReader reader = Context.newByteArrayObjectReader("RLPn", bytes);
        return Struct.readObject(reader);
    }

    public byte[] toBytes() {
        ByteArrayObjectWriter writer = Context.newByteArrayObjectWriter("RLPn");
        Struct.writeObject(writer, this);
        return writer.toByteArray();
    }

    //JsonObjectProcessor auto-detect parse and toJson functions without fixed name
    public static Struct parse(JsonValue jsonValue) {
        if (jsonValue == null || jsonValue.isNull()) {
            return null;
        }
        JsonObject jsonObject = jsonValue.asObject();
        Struct obj = new Struct();
        JsonValue valueJsonValue = jsonObject.get("value");
        if (valueJsonValue != null && !valueJsonValue.isNull()) {
            obj.setValue(valueJsonValue.asString());
        }
        return obj;
    }

    public static JsonValue toJson(Struct obj) {
        JsonObject jsonObject = Json.object();
        String value = obj.getValue();
        JsonValue valueJsonValue = value == null ? Json.NULL : Json.value(value);
        jsonObject.add("value", valueJsonValue);
        return jsonObject;
    }
}
