package io.dscope.camel.a2a.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import io.dscope.camel.a2a.config.A2AExchangeProperties;
import io.dscope.camel.a2a.config.A2AProtocolDefaults;

import java.util.LinkedHashMap;
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

        Map<String, Object> envelope = new LinkedHashMap<>();
        envelope.put("jsonrpc", A2AProtocolDefaults.JSONRPC_VERSION);
        envelope.put("result", result);
        envelope.put("id", id);
        exchange.getMessage().setHeader(Exchange.CONTENT_TYPE, "application/json");
        exchange.getMessage().setBody(mapper.writeValueAsString(envelope));
    }
}
