package foundation.icon.jsonrpc.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;
import java.util.Objects;

public class CallData {
    private String method;
    private Map<String, Object> params;

    public CallData(String method, Map<String, Object> params) {
        Objects.requireNonNull(method, "method required not null");
        if (method.isEmpty()) {
            throw new IllegalArgumentException("method required not empty");
        }
        this.method = method;
        this.params = params;
    }

    public String getMethod() {
        return method;
    }

    public Map<String, Object> getParams() {
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
