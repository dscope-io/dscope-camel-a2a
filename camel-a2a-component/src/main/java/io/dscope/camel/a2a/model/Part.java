package io.dscope.camel.a2a.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * A message or artifact content fragment.
 */
public class Part {

    private String partId;
    private String type;
    private String mimeType;
    private String raw;
    private String url;
    private String filename;
    private String text;
    private Object data;
    private Map<String, Object> metadata;

    @JsonIgnore
    public String getPartId() {
        return partId;
    }

    @JsonIgnore
    public void setPartId(String partId) {
        this.partId = partId;
    }

    @JsonIgnore
    public String getType() {
        return text != null ? "text" : data != null ? "data" : raw != null || url != null ? "file" : type;
    }

    @JsonIgnore
    public void setType(String type) {
        this.type = type;
    }

    @JsonProperty("mediaType")
    public String getMimeType() {
        return mimeType;
    }

    @JsonProperty("mediaType")
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getRaw() {
        return raw;
    }

    public void setRaw(String raw) {
        this.raw = raw;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}
