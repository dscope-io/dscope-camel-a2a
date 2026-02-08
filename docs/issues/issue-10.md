# Issue 10

**Title**: `[M4] Implement Agent Card discovery and GetExtendedAgentCard`  
**Labels**: `enhancement`, `discovery`, `security`, `a2a`, `phase-4`  
**Milestone**: `M4 - Discovery and Hardening`

### Description

Expose `/.well-known/agent-card.json` and protocol method for extended card retrieval, including declared capabilities and auth schemes.

### Tasks

- [ ] Build `AgentCardCatalog` and card builder.
- [ ] Add HTTP route for `/.well-known/agent-card.json`.
- [ ] Implement `GetExtendedAgentCard`.
- [ ] Add extension points for JWS signing/verification and policy checks.

### Acceptance Criteria

- [ ] Discovery endpoint returns valid card JSON.
- [ ] Extended card method returns additional fields as expected.
- [ ] Card capability/security sections are documented and test-covered.

### Dependencies

- Issues 5, 6

