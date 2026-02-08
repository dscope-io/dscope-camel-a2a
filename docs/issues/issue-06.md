# Issue 6

**Title**: `[M2] Implement core method processors: SendMessage, GetTask, ListTasks, CancelTask`  
**Labels**: `enhancement`, `protocol`, `a2a`, `phase-2`  
**Milestone**: `M2 - Core Protocol`

### Description

Add processors for baseline A2A task operations and wire them through method dispatch routes.

### Tasks

- [ ] Implement processor for `SendMessage`.
- [ ] Implement processor for `GetTask`.
- [ ] Implement processor for `ListTasks`.
- [ ] Implement processor for `CancelTask`.
- [ ] Add request validation per method.

### Acceptance Criteria

- [ ] Methods are callable via JSON-RPC route.
- [ ] Invalid params return `-32602`.
- [ ] Unknown method returns `-32601`.
- [ ] Method contract tests pass.

### Dependencies

- Issues 2, 3, 5

