# Issue 11

**Title**: `[M4] Replace sample routes with protocol-compliant HTTP + SSE service`  
**Labels**: `enhancement`, `samples`, `a2a`, `phase-4`  
**Milestone**: `M4 - Discovery and Hardening`

### Description

Rework sample service routes from echo/demo style to production-like protocol routing aligned with MCP sample quality.

### Tasks

- [ ] Add JSON-RPC HTTP route with envelope/method dispatch/error handling.
- [ ] Add SSE stream/subscribe routes.
- [ ] Add health and diagnostics endpoints.
- [ ] Provide curl examples for each implemented method.

### Acceptance Criteria

- [ ] Sample service demonstrates full supported protocol flow.
- [ ] Routes are documented and runnable from README.
- [ ] Manual smoke test checklist passes.

### Dependencies

- Issues 4, 6, 8, 10

