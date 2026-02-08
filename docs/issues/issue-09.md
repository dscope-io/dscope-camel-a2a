# Issue 9

**Title**: `[M3] Implement push notification config APIs`  
**Labels**: `enhancement`, `notifications`, `a2a`, `phase-3`  
**Milestone**: `M3 - Async and Streaming`

### Description

Implement create/get/list/delete push notification config methods and provide notifier service abstraction.

### Tasks

- [ ] Add push config models.
- [ ] Implement methods for create/get/list/delete.
- [ ] Add notifier interface + sample webhook notifier.
- [ ] Add retry/failure behavior and observability hooks.

### Acceptance Criteria

- [ ] All push config methods are callable and validated.
- [ ] Notification retries are bounded and configurable.
- [ ] Unit tests cover create/list/delete and error cases.

### Dependencies

- Issues 5, 7

