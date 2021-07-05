package com.iconloop.jsonrpc.model;

import com.iconloop.jsonrpc.Address;

public class CallParam {
    private Address to;
    private String dataType = "call";
    private CallData data;

    public CallParam(Address to, String method, Object params) {
        this(to, new CallData(method, params));
    }

    public CallParam(Address to, CallData data) {
        this.to = to;
        this.data = data;
    }

    public Address getTo() {
        return to;
    }

    public String getDataType() {
        return dataType;
    }

    public CallData getData() {
        return data;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CallParam{");
        sb.append("to=").append(to);
        sb.append(", dataType='").append(dataType).append('\'');
        sb.append(", data=").append(data);
        sb.append('}');
        return sb.toString();
    }
}
