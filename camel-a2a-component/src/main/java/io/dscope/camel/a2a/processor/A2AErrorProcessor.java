package io.dscope.camel.a2a.processor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import io.dscope.camel.a2a.JsonRpcError;
import io.dscope.camel.a2a.config.A2AExchangeProperties;

/**
 * Builds JSON-RPC 2.0 error envelopes from exchange failures.
 */
public class A2AErrorProcessor implements Processor {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void process(Exchange exchange) throws Exception {
        Throwable error = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Throwable.class);
        if (error == null) {
            error = exchange.getException();
        }
        if (error == null) {
            error = new IllegalStateException("Internal error");
        }

        int errorCode = mapErrorCode(error);
        Object requestId = resolveRequestId(exchange, errorCode);
        String message = resolveMessage(error, errorCode);
        String responseBody = JsonRpcError.envelope(requestId, errorCode, message, true);

        exchange.getMessage().setHeader(Exchange.CONTENT_TYPE, "application/json");
        exchange.getMessage().setBody(responseBody);
    }

    private int mapErrorCode(Throwable error) {
        if (error instanceof A2AMethodNotFoundException || error instanceof NoSuchMethodException) {
            return -32601;
        }
        if (error instanceof A2AInvalidParamsException) {
            return -32602;
        }
        if (error instanceof A2AJsonRpcValidationException || error instanceof IllegalArgumentException) {
            return -32600;
        }
        return -32603;
    }

    private Object resolveRequestId(Exchange exchange, int errorCode) {
        Object id = exchange.getProperty(A2AExchangeProperties.REQUEST_ID);
        if (id != null) {
            return id;
        }
        String rawPayload = exchange.getProperty(A2AExchangeProperties.RAW_PAYLOAD, String.class);
        Object extractedId = extractIdFromRawPayload(rawPayload);
        if (extractedId != null) {
            return extractedId;
        }
        if (errorCode == -32600 || errorCode == -32601 || errorCode == -32602 || errorCode == -32603) {
            return null;
        }
        return null;
    }

    private Object extractIdFromRawPayload(String rawPayload) {
        if (rawPayload == null || rawPayload.isBlank()) {
            return null;
        }
        try {
            JsonNode root = mapper.readTree(rawPayload);
            JsonNode idNode = root.get("id");
            if (idNode == null || idNode.isNull()) {
                return null;
            }
            if (idNode.isTextual()) {
                return idNode.asText();
            }
            if (idNode.isNumber()) {
                return idNode.numberValue();
            }
            if (idNode.isBoolean()) {
                return idNode.booleanValue();
            }
            return idNode.toString();
        } catch (Exception ignored) {
            return null;
        }
    }

    private String resolveMessage(Throwable error, int errorCode) {
        String message = error.getMessage();
        if (message != null && !message.isBlank()) {
            return message;
        }
        return switch (errorCode) {
            case -32600 -> "Invalid Request";
            case -32601 -> "Method not found";
            case -32602 -> "Invalid params";
            default -> "Internal error";
        };
    }
}
