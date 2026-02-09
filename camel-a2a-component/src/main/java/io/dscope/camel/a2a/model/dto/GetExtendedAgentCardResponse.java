package io.dscope.camel.a2a.model.dto;

import io.dscope.camel.a2a.model.AgentCard;

/**
 * Result payload for GetExtendedAgentCard.
 */
public class GetExtendedAgentCardResponse {

    private AgentCard agentCard;
    private String signature;

    public AgentCard getAgentCard() {
        return agentCard;
    }

    public void setAgentCard(AgentCard agentCard) {
        this.agentCard = agentCard;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
