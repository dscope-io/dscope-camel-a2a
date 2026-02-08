# Issue 4

**Title**: `[M1] Add A2AComponentApplicationSupport bootstrap class`  
**Labels**: `enhancement`, `developer-experience`, `a2a`, `phase-1`  
**Milestone**: `M1 - Foundation`

### Description

Introduce MCP-style application support class for sample/services bootstrapping:

- Binds default processors/services to Camel registry
- Configures route include patterns
- Centralizes default wiring

### Tasks

- [ ] Implement `A2AComponentApplicationSupport`.
- [ ] Bind default beans (envelope, error, method processors).
- [ ] Add extension hooks for sample-specific processors.
- [ ] Update sample runner to use this support class.

### Acceptance Criteria

- [ ] Samples can boot through one standardized bootstrap path.
- [ ] Bean wiring is centralized and testable.
- [ ] Route include pattern validation exists.

### Dependencies

- Issues 1, 2, 3

