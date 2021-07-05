package com.iconloop.jsonrpc.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.iconloop.jsonrpc.Address;

import java.math.BigInteger;

public abstract class AbstractTransaction {
    protected BigInteger version = new BigInteger("3");
    protected Address from;
    protected Address to;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected BigInteger value;
    protected BigInteger timestamp;
    protected BigInteger nid;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected BigInteger nonce;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected String dataType;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected Object data;

    public BigInteger getVersion() {
        return version;
    }

    public Address getFrom() {
        return from;
    }

    public Address getTo() {
        return to;
    }

    public BigInteger getValue() {
        return value;
    }

    public BigInteger getTimestamp() {
        return timestamp;
    }

    public BigInteger getNid() {
        return nid;
    }

    public BigInteger getNonce() {
        return nonce;
    }

    public String getDataType() {
        return dataType;
    }

    public Object getData() {
        return data;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AbstractTransaction{");
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
