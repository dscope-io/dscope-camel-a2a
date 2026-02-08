# Issue 2

**Title**: `[M1] Add A2A JSON-RPC envelope parser and validator processor`  
**Labels**: `enhancement`, `protocol`, `a2a`, `phase-1`  
**Milestone**: `M1 - Foundation`

### Description

Implement a processor equivalent to MCP envelope handling that:

- Parses JSON-RPC 2.0 payloads
- Validates required fields
- Distinguishes request/notification/response semantics
- Extracts method and id into exchange properties
- Normalizes params for downstream processors

### Tasks

- [ ] Add `A2AJsonRpcEnvelopeProcessor`.
- [ ] Define exchange property names for type/id/method/raw payload.
- [ ] Validate `jsonrpc == "2.0"` and payload object shape.
- [ ] Add method whitelist support for current phase.

### Acceptance Criteria

- [ ] Invalid envelopes return deterministic errors.
- [ ] Valid envelopes set exchange properties consistently.
- [ ] Unit tests cover malformed JSON, missing fields, and happy path.

### Dependencies

- Issue 1

