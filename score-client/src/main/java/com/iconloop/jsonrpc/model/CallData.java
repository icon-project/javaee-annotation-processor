package com.iconloop.jsonrpc.model;

import com.fasterxml.jackson.annotation.JsonInclude;

public class CallData {
    private String method;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Object params;

    public CallData(String method, Object params) {
        this.method = method;
        this.params = params;
    }

    public String getMethod() {
        return method;
    }

    public Object getParams() {
        return params;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CallData{");
        sb.append("method='").append(method).append('\'');
        sb.append(", params=").append(params);
        sb.append('}');
        return sb.toString();
    }
}
