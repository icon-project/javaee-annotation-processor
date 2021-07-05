package com.iconloop.jsonrpc.model;

import com.iconloop.jsonrpc.Address;

import java.math.BigInteger;

public class TransactionParam extends AbstractTransaction {
    public static final BigInteger TIMESTAMP_MSEC_SCALE = BigInteger.valueOf(1000);
    public static BigInteger currentTimestamp() {
        return TIMESTAMP_MSEC_SCALE.multiply(BigInteger.valueOf(System.currentTimeMillis()));
    }

    public TransactionParam(BigInteger nid, Address to, BigInteger value, String dataType, Object data) {
        super();
        this.nid = nid;
        this.to = to;
        this.value = value;
        this.dataType = dataType;
        this.data = data;
    }

    public void setFrom(Address from) {
        this.from = from;
    }

    public void setTimestamp(BigInteger timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TransactionParam{");
        sb.append("version=").append(version);
        sb.append(", from=").append(from);
        sb.append(", to=").append(to);
        sb.append(", value=").append(value);
        sb.append(", timestamp=").append(timestamp);
        sb.append(", nid=").append(nid);
        sb.append(", nonce=").append(nonce);
        sb.append(", dataType='").append(dataType).append('\'');
        sb.append(", data=").append(data);
        sb.append('}');
        return sb.toString();
    }
}
