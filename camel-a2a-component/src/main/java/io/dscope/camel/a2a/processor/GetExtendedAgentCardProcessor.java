package io.dscope.camel.a2a.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import io.dscope.camel.a2a.catalog.AgentCardCatalog;
import io.dscope.camel.a2a.config.A2AExchangeProperties;
import io.dscope.camel.a2a.model.AgentCard;
import io.dscope.camel.a2a.model.dto.GetExtendedAgentCardRequest;
import io.dscope.camel.a2a.model.dto.GetExtendedAgentCardResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles GetExtendedAgentCard method invocation.
 */
public class GetExtendedAgentCardProcessor implements Processor {

    private static final Logger LOG = LoggerFactory.getLogger(GetExtendedAgentCardProcessor.class);

    private final ObjectMapper mapper = new ObjectMapper();
    private final AgentCardCatalog cardCatalog;

    public GetExtendedAgentCardProcessor(AgentCardCatalog cardCatalog) {
        this.cardCatalog = cardCatalog;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        try {
            Object params = exchange.getProperty(A2AExchangeProperties.NORMALIZED_PARAMS);
            GetExtendedAgentCardRequest request =
                params == null ? new GetExtendedAgentCardRequest() : mapper.convertValue(params, GetExtendedAgentCardRequest.class);

            AgentCard card = cardCatalog.getExtendedCard();
            boolean includeSignature = request.getIncludeSignature() == null || request.getIncludeSignature();

            LOG.debug("Processing extended agent card request for exchangeId={}, includeSignature={}",
                exchange.getExchangeId(),
                includeSignature);

            GetExtendedAgentCardResponse response = new GetExtendedAgentCardResponse();
            response.setAgentCard(card);
            if (includeSignature) {
                response.setSignature(cardCatalog.getCardSignature(card));
            }
            exchange.setProperty(A2AExchangeProperties.METHOD_RESULT, response);
            LOG.debug("Extended agent card response prepared for exchangeId={}, signatureIncluded={}",
                exchange.getExchangeId(),
                includeSignature);
        } catch (Exception e) {
            LOG.error("Failed to process extended agent card request for exchangeId={}", exchange.getExchangeId(), e);
            throw e;
        }
    }
}
