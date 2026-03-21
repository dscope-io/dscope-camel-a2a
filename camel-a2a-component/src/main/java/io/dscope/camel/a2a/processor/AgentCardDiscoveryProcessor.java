package io.dscope.camel.a2a.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import io.dscope.camel.a2a.catalog.AgentCardCatalog;
import io.dscope.camel.a2a.model.AgentCard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Serves discovery card JSON for the well-known endpoint.
 */
public class AgentCardDiscoveryProcessor implements Processor {

    private static final Logger LOG = LoggerFactory.getLogger(AgentCardDiscoveryProcessor.class);

    private final ObjectMapper mapper = new ObjectMapper();
    private final AgentCardCatalog cardCatalog;

    public AgentCardDiscoveryProcessor(AgentCardCatalog cardCatalog) {
        this.cardCatalog = cardCatalog;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        try {
            LOG.debug("Processing discovery card request for exchangeId={}", exchange.getExchangeId());
            AgentCard card = cardCatalog.getDiscoveryCard();
            String signature = cardCatalog.getCardSignature(card);
            if (signature != null) {
                exchange.getMessage().setHeader("X-A2A-AgentCard-Signature", signature);
            }
            exchange.getMessage().setHeader(Exchange.CONTENT_TYPE, "application/json");
            exchange.getMessage().setBody(mapper.writeValueAsString(card));
            LOG.debug("Discovery card response written for exchangeId={}, signaturePresent={}",
                exchange.getExchangeId(),
                signature != null);
        } catch (Exception e) {
            LOG.error("Failed to process discovery card request for exchangeId={}", exchange.getExchangeId(), e);
            throw e;
        }
    }
}
