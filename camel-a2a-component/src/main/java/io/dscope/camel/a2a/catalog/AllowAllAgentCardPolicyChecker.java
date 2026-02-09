package io.dscope.camel.a2a.catalog;

import io.dscope.camel.a2a.model.AgentCard;

/**
 * Default checker that allows all cards.
 */
public class AllowAllAgentCardPolicyChecker implements AgentCardPolicyChecker {

    @Override
    public void validate(AgentCard card) {
        // Default no-op.
    }
}
