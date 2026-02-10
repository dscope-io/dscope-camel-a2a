package io.dscope.camel.a2a.processor;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.io.InputStream;
import java.io.Reader;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import io.dscope.camel.a2a.config.A2AExchangeProperties;
import io.dscope.camel.a2a.config.A2AProtocolMethods;

/**
 * Parses and validates JSON-RPC 2.0 envelopes.
 */
public class A2AJsonRpcEnvelopeProcessor implements Processor {

    private final ObjectMapper mapper;
    private final Set<String> allowedMethods;

    public A2AJsonRpcEnvelopeProcessor() {
        this(A2AProtocolMethods.CORE_METHODS);
    }

    public A2AJsonRpcEnvelopeProcessor(Set<String> allowedMethods) {
        this.mapper = new ObjectMapper();
        this.allowedMethods = allowedMethods == null ? Collections.emptySet() : Set.copyOf(allowedMethods);
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        Object body = exchange.getIn().getBody();
        JsonNode root = parseEnvelope(exchange, body);
        String rawPayload = rawPayload(body, root);
        Object requestId = resolveId(root.get("id"));

        exchange.setProperty(A2AExchangeProperties.RAW_PAYLOAD, rawPayload);
        exchange.setProperty(A2AExchangeProperties.REQUEST_ID, requestId);
        validateBaseEnvelope(root);

        String envelopeType = determineEnvelopeType(root);
        String method = root.has("method") && !root.get("method").isNull() ? root.get("method").asText() : null;
        Object normalizedParams = normalizeParams(root, envelopeType);

        exchange.setProperty(A2AExchangeProperties.ENVELOPE_TYPE, envelopeType);
        exchange.setProperty(A2AExchangeProperties.METHOD, method);
        exchange.setProperty(A2AExchangeProperties.NORMALIZED_PARAMS, normalizedParams);
        exchange.getMessage().setBody(root);
    }

    private JsonNode parseEnvelope(Exchange exchange, Object body) {
        try {
            if (body == null) {
                throw new A2AJsonRpcValidationException("Invalid JSON-RPC envelope: body must not be null");
            }
            if (body instanceof JsonNode jsonNode) {
                return jsonNode;
            }
            if (body instanceof CharSequence jsonString) {
                return mapper.readTree(jsonString.toString());
            }
            if (body instanceof byte[] bytes) {
                return mapper.readTree(bytes);
            }
            if (body instanceof InputStream inputStream) {
                return mapper.readTree(inputStream);
            }
            if (body instanceof Reader reader) {
                return mapper.readTree(reader);
            }
            if (body instanceof Map<?, ?> || body instanceof List<?>) {
                return mapper.valueToTree(body);
            }

            String coerced = exchange.getIn().getBody(String.class);
            if (coerced != null && !coerced.isBlank()) {
                return mapper.readTree(coerced);
            }
            return mapper.valueToTree(body);
        } catch (A2AJsonRpcValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new A2AJsonRpcValidationException("Malformed JSON-RPC payload", e);
        }
    }

    private void validateBaseEnvelope(JsonNode root) {
        if (!root.isObject()) {
            throw new A2AJsonRpcValidationException("Invalid JSON-RPC envelope: payload must be an object");
        }
        if (!root.hasNonNull("jsonrpc") || !"2.0".equals(root.get("jsonrpc").asText())) {
            throw new A2AJsonRpcValidationException("Invalid JSON-RPC envelope: jsonrpc must be \"2.0\"");
        }
    }

    private String determineEnvelopeType(JsonNode root) {
        boolean hasMethod = root.has("method");
        boolean hasId = root.has("id");
        boolean hasResult = root.has("result");
        boolean hasError = root.has("error");

        if (hasMethod) {
            if (!root.get("method").isTextual() || root.get("method").asText().isBlank()) {
                throw new A2AJsonRpcValidationException("Invalid JSON-RPC envelope: method must be a non-empty string");
            }
            if (hasResult || hasError) {
                throw new A2AJsonRpcValidationException(
                    "Invalid JSON-RPC envelope: request/notification must not contain result or error");
            }
            String method = root.get("method").asText();
            if (!allowedMethods.isEmpty() && !allowedMethods.contains(method)) {
                throw new A2AMethodNotFoundException("Method not found: " + method);
            }
            return hasId ? "request" : "notification";
        }

        if (hasResult == hasError) {
            throw new A2AJsonRpcValidationException(
                "Invalid JSON-RPC envelope: response must contain exactly one of result or error");
        }
        if (!hasId) {
            throw new A2AJsonRpcValidationException("Invalid JSON-RPC envelope: response must contain id");
        }
        return hasError ? "error" : "response";
    }

    private Object normalizeParams(JsonNode root, String envelopeType) {
        JsonNode normalizedNode;
        if ("request".equals(envelopeType) || "notification".equals(envelopeType)) {
            normalizedNode = root.get("params");
        } else if ("response".equals(envelopeType)) {
            normalizedNode = root.get("result");
        } else {
            normalizedNode = root.get("error");
        }
        if (normalizedNode == null || normalizedNode.isNull()) {
            return null;
        }
        return mapper.convertValue(normalizedNode, Object.class);
    }

    private String rawPayload(Object originalBody, JsonNode root) throws Exception {
        if (originalBody instanceof String s) {
            return s;
        }
        return mapper.writeValueAsString(root);
    }

    private Object resolveId(JsonNode idNode) {
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
    }
}
