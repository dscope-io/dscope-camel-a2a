# Issue 1

**Title**: `[M1] Refactor package structure to MCP-aligned architecture`  
**Labels**: `enhancement`, `architecture`, `a2a`, `phase-1`  
**Milestone**: `M1 - Foundation`

### Description

Restructure `camel-a2a-component` code into modular packages aligned with `CamelMcpComponent` patterns:

- `model`
- `processor`
- `catalog`
- `service`
- `config`

Move existing classes to appropriate packages and keep backward compatibility where feasible.

### Tasks

- [ ] Create package directories and migrate existing classes.
- [ ] Introduce shared constants class for exchange property keys.
- [ ] Remove ad hoc protocol logic from producer/consumer classes.
- [ ] Update imports and tests.

### Acceptance Criteria

- [ ] Project compiles without package conflicts.
- [ ] Existing tests pass after migration.
- [ ] No behavior regression in currently supported flows.

### Dependencies

- None

