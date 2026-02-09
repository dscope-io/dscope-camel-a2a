package io.dscope.camel.a2a.catalog;

import io.dscope.camel.a2a.model.AgentCard;

/**
 * Catalog abstraction for discovery and extended agent cards.
 */
public interface AgentCardCatalog {

    AgentCard getDiscoveryCard();

    AgentCard getExtendedCard();

    String getCardSignature(AgentCard card);
}
