package foundation.icon.jsonrpc.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import foundation.icon.jsonrpc.IconJsonModule;


public class Hash {
    public static final String HEX_PREFIX = "0x";

    private final byte[] bytes;

    public Hash(byte[] bytes) {
        if (bytes == null) {
            throw new IllegalArgumentException("bytes could not be null");
        }
        this.bytes = bytes;
    }

    @JsonCreator
    public Hash(String string) {
        if (string == null) {
            throw new IllegalArgumentException("string could not be null");
        }
        if (string.startsWith(HEX_PREFIX)) {
            string = string.substring(2);
        }
        this.bytes = IconJsonModule.hexToBytes(string);
    }

    public byte[] toBytes() {
        return bytes;
    }

    @JsonValue
    @Override
    public String toString() {
        return HEX_PREFIX + IconJsonModule.bytesToHex(bytes);
    }
}
