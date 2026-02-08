package io.dscope.camel.a2a;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

/**
 * Processor that routes A2A intent messages.
 * Extracts the method from the incoming A2AMessage and creates a response
 * indicating the intent was handled along with the method name.
 */
public class IntentRouterProcessor implements Processor {

    /**
     * Processes the exchange by extracting the A2A message and creating
     * a response with handling confirmation.
     *
     * @param ex the Camel exchange to process
     * @throws Exception if processing fails
     */
    @Override
    public void process(Exchange ex) throws Exception {
        Object body = ex.getIn().getBody();
        ObjectMapper mapper = new ObjectMapper();

        // Parse the body as A2AMessage, handling both direct objects and JSON strings
        A2AMessage msg = body instanceof A2AMessage
            ? (A2AMessage) body
            : mapper.readValue(body != null ? body.toString() : "{}", A2AMessage.class);

        // Create response indicating the intent was handled
        ex.getMessage().setBody(mapper.writeValueAsString(
            Map.of("handled", true, "method", msg.getMethod())));
    }
}
