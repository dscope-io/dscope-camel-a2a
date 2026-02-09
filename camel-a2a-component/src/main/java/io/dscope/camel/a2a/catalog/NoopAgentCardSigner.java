package io.dscope.camel.a2a.catalog;

/**
 * Default signer that does not emit a signature.
 */
public class NoopAgentCardSigner implements AgentCardSigner {

    @Override
    public String sign(String canonicalCardJson) {
        return null;
    }
}
