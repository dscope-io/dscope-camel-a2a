package io.dscope.camel.a2a.catalog;

import io.dscope.camel.a2a.model.AgentCard;

/**
 * Extension point for policy validation over card content.
 */
public interface AgentCardPolicyChecker {

    void validate(AgentCard card);
}
