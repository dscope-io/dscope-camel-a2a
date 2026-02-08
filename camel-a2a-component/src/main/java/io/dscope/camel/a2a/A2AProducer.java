package io.dscope.camel.a2a;

import org.apache.camel.Exchange;
import org.apache.camel.support.DefaultProducer;

/**
 * Producer for sending messages to A2A endpoints.
 * Converts incoming exchange bodies into A2A intent execution requests.
 */
public class A2AProducer extends DefaultProducer {

    /** The endpoint this producer belongs to */
    @SuppressWarnings("unused")
    private final A2AEndpoint ep;

    /** JSON codec for message serialization */
    private final A2AJsonCodec codec = new A2AJsonCodec();

    /**
     * Creates a new A2A producer.
     *
     * @param ep the parent endpoint
     */
    public A2AProducer(A2AEndpoint ep) {
        super(ep);
        this.ep = ep;
    }

    /**
     * Processes the exchange by creating an A2A intent execution request
     * and serializing it to JSON.
     *
     * @param ex the exchange to process
     * @throws Exception if processing fails
     */
    @Override
    public void process(Exchange ex) throws Exception {
        A2AMessage msg = A2AMessage.request("intent/execute", ex.getIn().getBody());
        String json = codec.serialize(msg);
        ex.getMessage().setBody(json);
    }
}
