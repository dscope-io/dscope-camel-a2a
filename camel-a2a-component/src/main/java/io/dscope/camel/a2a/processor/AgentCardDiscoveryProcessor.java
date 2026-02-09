package io.dscope.camel.a2a.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import io.dscope.camel.a2a.catalog.AgentCardCatalog;
import io.dscope.camel.a2a.model.AgentCard;

/**
 * Serves discovery card JSON for the well-known endpoint.
 */
public class AgentCardDiscoveryProcessor implements Processor {

    private final ObjectMapper mapper = new ObjectMapper();
    private final AgentCardCatalog cardCatalog;

    public AgentCardDiscoveryProcessor(AgentCardCatalog cardCatalog) {
        this.cardCatalog = cardCatalog;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        AgentCard card = cardCatalog.getDiscoveryCard();
        String signature = cardCatalog.getCardSignature(card);
        if (signature != null) {
            exchange.getMessage().setHeader("X-A2A-AgentCard-Signature", signature);
        }
        exchange.getMessage().setHeader(Exchange.CONTENT_TYPE, "application/json");
        exchange.getMessage().setBody(mapper.writeValueAsString(card));
    }
}
