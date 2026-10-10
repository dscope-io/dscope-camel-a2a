package io.dscope.camel.a2a.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import io.dscope.camel.a2a.config.A2AExchangeProperties;
import io.dscope.camel.a2a.config.A2AProtocolDefaults;
import io.dscope.camel.a2a.config.A2AProtocolMethods;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Dispatches parsed JSON-RPC requests to method-specific processors.
 */
public class A2AMethodDispatchProcessor implements Processor {

    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<String, Processor> methodProcessors;

    public A2AMethodDispatchProcessor(Map<String, Processor> methodProcessors) {
        this.methodProcessors = Map.copyOf(methodProcessors);
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        String envelopeType = Objects.toString(exchange.getProperty(A2AExchangeProperties.ENVELOPE_TYPE), "");
        if (!"request".equals(envelopeType) && !"notification".equals(envelopeType)) {
            throw new A2AJsonRpcValidationException("Only request/notification envelopes can be dispatched");
        }

        String method = exchange.getProperty(A2AExchangeProperties.METHOD, String.class);
        Processor methodProcessor = methodProcessors.get(method);
        if (methodProcessor == null) {
            throw new A2AMethodNotFoundException("Method not found: " + method);
        }

        methodProcessor.process(exchange);

        if ("notification".equals(envelopeType)) {
            exchange.getMessage().setBody("");
            return;
        }

        Object id = exchange.getProperty(A2AExchangeProperties.REQUEST_ID);
        Object result = exchange.getProperty(A2AExchangeProperties.METHOD_RESULT);

        if (A2AProtocolMethods.SEND_STREAMING_MESSAGE.equals(method)
            || A2AProtocolMethods.SUBSCRIBE_TO_TASK.equals(method)) {
            if (!(result instanceof List<?> streamResponses)) {
                throw new IllegalStateException("A2A stream response requires a list of stream events");
            }
            StringBuilder streamBody = new StringBuilder();
            for (Object streamResponse : streamResponses) {
                Map<String, Object> streamEnvelope = new LinkedHashMap<>();
                streamEnvelope.put("jsonrpc", A2AProtocolDefaults.JSONRPC_VERSION);
                streamEnvelope.put("result", streamResponse);
                streamEnvelope.put("id", id);
                streamBody.append("data: ").append(mapper.writeValueAsString(streamEnvelope)).append("\n\n");
            }
            exchange.getMessage().setHeader(Exchange.CONTENT_TYPE, "text/event-stream");
            exchange.getMessage().setBody(streamBody.toString());
            return;
        }

        Map<String, Object> envelope = new LinkedHashMap<>();
        envelope.put("jsonrpc", A2AProtocolDefaults.JSONRPC_VERSION);
        envelope.put("result", result);
        envelope.put("id", id);
        exchange.getMessage().setHeader(Exchange.CONTENT_TYPE, A2AProtocolDefaults.JSON_CONTENT_TYPE);
        exchange.getMessage().setBody(mapper.writeValueAsString(envelope));
    }
}
