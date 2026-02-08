# Issue 7

**Title**: `[M2] Implement stateful task service with lifecycle enforcement`  
**Labels**: `enhancement`, `state-management`, `a2a`, `phase-2`  
**Milestone**: `M2 - Core Protocol`

### Description

Create a task service/registry that manages lifecycle transitions and task history with protocol-safe state changes.

### Tasks

- [ ] Add `A2ATaskService` (in-memory first; interface-based for pluggable storage).
- [ ] Implement legal state transitions and terminal checks.
- [ ] Add idempotency handling for repeated request/message IDs.
- [ ] Add tests for transition matrix and cancellation paths.

### Acceptance Criteria

- [ ] Illegal transitions are rejected with clear errors.
- [ ] Task history is retrievable for diagnostics.
- [ ] Concurrency behavior is deterministic under parallel updates.

### Dependencies

- Issues 5, 6

