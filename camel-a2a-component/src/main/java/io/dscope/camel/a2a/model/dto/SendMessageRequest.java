package io.dscope.camel.a2a.model.dto;

import java.util.Map;
import io.dscope.camel.a2a.model.Message;

/**
 * Parameters for SendMessage.
 */
public class SendMessageRequest {

    private Message message;
    private String conversationId;
    private String idempotencyKey;
    private Map<String, Object> metadata;

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}
