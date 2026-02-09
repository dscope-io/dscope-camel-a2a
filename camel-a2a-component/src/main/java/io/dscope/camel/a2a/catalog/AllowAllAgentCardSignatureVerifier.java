package io.dscope.camel.a2a.catalog;

/**
 * Default verifier that accepts all signatures.
 */
public class AllowAllAgentCardSignatureVerifier implements AgentCardSignatureVerifier {

    @Override
    public boolean verify(String canonicalCardJson, String signature) {
        return true;
    }
}
