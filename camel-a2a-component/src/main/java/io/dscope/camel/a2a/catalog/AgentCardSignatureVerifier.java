package io.dscope.camel.a2a.catalog;

/**
 * Extension point for card signature verification.
 */
public interface AgentCardSignatureVerifier {

    boolean verify(String canonicalCardJson, String signature);
}
