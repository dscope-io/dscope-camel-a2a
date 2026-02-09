package io.dscope.camel.a2a.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import io.dscope.camel.a2a.config.A2AExchangeProperties;
import io.dscope.camel.a2a.model.A2AMessage;
import io.dscope.camel.a2a.service.A2AJsonCodec;
import io.dscope.camel.a2a.service.A2ARequestFactory;

/**
 * Converts an exchange body into an outbound A2A JSON-RPC payload.
 */
public class A2AOutgoingMessageProcessor implements Processor {

    private final A2ARequestFactory requestFactory;
    private final A2AJsonCodec codec;

    public A2AOutgoingMessageProcessor() {
        this(new A2ARequestFactory(), new A2AJsonCodec());
    }

    public A2AOutgoingMessageProcessor(A2ARequestFactory requestFactory, A2AJsonCodec codec) {
        this.requestFactory = requestFactory;
        this.codec = codec;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        Object inboundBody = exchange.getIn().getBody();
        A2AMessage message = requestFactory.createRequest(inboundBody);
        String json = codec.serialize(message);

        exchange.setProperty(A2AExchangeProperties.ENVELOPE_TYPE, "request");
        exchange.setProperty(A2AExchangeProperties.REQUEST_ID, message.getId());
        exchange.setProperty(A2AExchangeProperties.METHOD, message.getMethod());
        exchange.setProperty(A2AExchangeProperties.NORMALIZED_PARAMS, message.getParams());
        exchange.setProperty(A2AExchangeProperties.RAW_PAYLOAD, json);
        exchange.getMessage().setBody(json);
    }
}
