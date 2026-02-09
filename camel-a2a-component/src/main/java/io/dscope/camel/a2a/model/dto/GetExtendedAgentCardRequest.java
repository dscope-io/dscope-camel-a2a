package io.dscope.camel.a2a.model.dto;

/**
 * Parameters for GetExtendedAgentCard.
 */
public class GetExtendedAgentCardRequest {

    private Boolean includeSignature;

    public Boolean getIncludeSignature() {
        return includeSignature;
    }

    public void setIncludeSignature(Boolean includeSignature) {
        this.includeSignature = includeSignature;
    }
}
