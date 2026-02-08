# Issue 5

**Title**: `[M2] Implement core A2A domain models (Task, Message, Part, Artifact, AgentCard)`  
**Labels**: `enhancement`, `model`, `a2a`, `phase-2`  
**Milestone**: `M2 - Core Protocol`

### Description

Replace minimal request-only message model with typed A2A domain models matching latest protocol structures.

### Tasks

- [ ] Add models for `Task`, `TaskStatus`, `Message`, `Part`, `Artifact`.
- [ ] Add models for `AgentCard` and capabilities/security metadata.
- [ ] Add request/response DTOs for core methods.
- [ ] Add serialization/deserialization tests for all models.

### Acceptance Criteria

- [ ] Models map cleanly to JSON payload examples.
- [ ] Backward-incompatible fields are intentionally versioned/documented.
- [ ] Unit tests cover round-trip serialization.

### Dependencies

- Issue 1

