# Issue 8

**Title**: `[M3] Add SendStreamingMessage and SubscribeToTask over SSE`  
**Labels**: `enhancement`, `streaming`, `sse`, `a2a`, `phase-3`  
**Milestone**: `M3 - Async and Streaming`

### Description

Implement asynchronous streaming support over SSE for task updates and method streaming responses.

### Tasks

- [ ] Add `SendStreamingMessage` support.
- [ ] Add `SubscribeToTask` support.
- [ ] Add SSE event publisher and subscriber lifecycle handling.
- [ ] Implement terminal-state completion behavior.
- [ ] Add timeout/backpressure protections.

### Acceptance Criteria

- [ ] SSE clients receive ordered updates for target tasks.
- [ ] Subscriptions close cleanly on terminal states.
- [ ] Integration tests validate connect/stream/complete.

### Dependencies

- Issue 7

