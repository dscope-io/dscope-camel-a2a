package io.dscope.camel.a2a;

import java.util.LinkedHashMap;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Utility class for creating JSON-RPC 2.0 error response envelopes.
 * Provides a static method to generate properly formatted error messages.
 */
public final class JsonRpcError {

    /**
     * Creates a JSON-RPC 2.0 error response envelope.
     * If JSON serialization fails, falls back to manual string construction.
     *
     * @param id the request id (can be null)
     * @param code the error code
     * @param message the error message
     * @return JSON string representing the error response
     */
    public static String envelope(String id, int code, String message) {
        try {
            var root = new LinkedHashMap<String, Object>();
            root.put("jsonrpc", "2.0");
            root.put("error", Map.of("code", code, "message", message));
            if (id != null) {
                root.put("id", id);
            }
            return new ObjectMapper().writeValueAsString(root);
        } catch (Exception e) {
            // Fallback to manual JSON construction if serialization fails
            return "{\"jsonrpc\":\"2.0\",\"error\":{\"code\":" + code + ",\"message\":\"" + message + "\"}}";
        }
    }
}
