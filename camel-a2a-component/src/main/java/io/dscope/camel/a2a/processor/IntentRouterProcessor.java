package io.dscope.camel.a2a.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import io.dscope.camel.a2a.model.A2AMessage;
import io.dscope.camel.a2a.config.A2AExchangeProperties;
import io.dscope.camel.a2a.config.A2AProtocolMethods;

/**
 * Processor that routes A2A intent-style messages.
 */
public class IntentRouterProcessor implements Processor {

    @Override
    public void process(Exchange ex) throws Exception {
        String method = ex.getProperty(A2AExchangeProperties.METHOD, String.class);
        if (A2AProtocolMethods.LEGACY_INTENT_EXECUTE.equals(method)) {
            ex.setProperty(A2AExchangeProperties.METHOD_RESULT, Map.of("handled", true, "method", method));
            return;
        }

        Object body = ex.getIn().getBody();
        ObjectMapper mapper = new ObjectMapper();

        A2AMessage msg = body instanceof A2AMessage
            ? (A2AMessage) body
            : mapper.readValue(body != null ? body.toString() : "{}", A2AMessage.class);

        ex.getMessage().setBody(mapper.writeValueAsString(
            Map.of("handled", true, "method", msg.getMethod())));
    }
}
