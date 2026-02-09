package io.dscope.camel.a2a.catalog;

/**
 * Extension point for card signing.
 */
public interface AgentCardSigner {

    String sign(String canonicalCardJson);
}
