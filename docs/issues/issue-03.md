# Issue 3

**Title**: `[M1] Implement standardized JSON-RPC error processor for A2A`  
**Labels**: `enhancement`, `protocol`, `a2a`, `phase-1`  
**Milestone**: `M1 - Foundation`

### Description

Create a reusable error processor that emits JSON-RPC 2.0 compliant error envelopes and maps validation/execution failures to correct error codes.

### Tasks

- [ ] Add `A2AErrorProcessor`.
- [ ] Define error code mapping strategy (`-32600`, `-32601`, `-32602`, `-32603`).
- [ ] Ensure ID echoing behavior follows JSON-RPC rules.
- [ ] Replace direct/manual error JSON creation in routes/processors.

### Acceptance Criteria

- [ ] Error envelopes are spec-conformant.
- [ ] Error messages are deterministic and test-covered.
- [ ] Route-level `onException` uses shared error processor.

### Dependencies

- Issue 2

