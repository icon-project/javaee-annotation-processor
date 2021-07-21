package foundation.icon.jsonrpc.model;

import foundation.icon.jsonrpc.IconJsonModule;

import java.util.Map;
import java.util.Objects;

public class DeployData {
    private String contentType;
    private byte[] content;
    private Map<String, Object> params;

    public DeployData(String contentType, byte[] content, Map<String, Object> params) {
        Objects.requireNonNull(contentType, "contentType required not null");
        if (contentType.isEmpty()) {
            throw new IllegalArgumentException("contentType required not empty");
        }
        Objects.requireNonNull(content, "content required not null");
        if (content.length == 0) {
            throw new IllegalArgumentException("content required not empty");
        }
        this.contentType = contentType;
        this.content = content;
        this.params = params;
    }

    public String getContentType() {
        return contentType;
    }

    public byte[] getContent() {
        return content;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DeployData{");
        sb.append("contentType='").append(contentType).append('\'');
        sb.append(", content=").append(IconJsonModule.bytesToHex(content));
        sb.append(", params=").append(params);
        sb.append('}');
        return sb.toString();
    }
}
