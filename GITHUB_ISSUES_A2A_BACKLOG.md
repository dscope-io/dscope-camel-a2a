# GitHub Issues Backlog: Camel A2A Protocol Compatibility

Last updated: 2026-02-07  
Source plan: `/Users/roman/Projects/DScope/CamelA2AComponent/A2A_COMPATIBILITY_PLAN.md`

## Recommended Milestones

- `M1 - Foundation`
- `M2 - Core Protocol`
- `M3 - Async and Streaming`
- `M4 - Discovery and Hardening`

## Issue 1

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

## Issue 2

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

## Issue 3

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

## Issue 4

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

## Issue 5

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

## Issue 6

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

## Issue 7

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

## Issue 8

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

## Issue 9

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

## Issue 10

**Title**: `[M4] Implement Agent Card discovery and GetExtendedAgentCard`  
**Labels**: `enhancement`, `discovery`, `security`, `a2a`, `phase-4`  
**Milestone**: `M4 - Discovery and Hardening`

### Description

Expose `/.well-known/agent-card.json` and protocol method for extended card retrieval, including declared capabilities and auth schemes.

### Tasks

- [ ] Build `AgentCardCatalog` and card builder.
- [ ] Add HTTP route for `/.well-known/agent-card.json`.
- [ ] Implement `GetExtendedAgentCard`.
- [ ] Add extension points for JWS signing/verification and policy checks.

### Acceptance Criteria

- [ ] Discovery endpoint returns valid card JSON.
- [ ] Extended card method returns additional fields as expected.
- [ ] Card capability/security sections are documented and test-covered.

### Dependencies

- Issues 5, 6

## Issue 11

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

## Issue 12

**Title**: `[M4] Add protocol contract test suite and documentation set`  
**Labels**: `enhancement`, `testing`, `documentation`, `a2a`, `phase-4`  
**Milestone**: `M4 - Discovery and Hardening`

### Description

Create complete test and documentation parity with MCP component quality level.

### Tasks

- [ ] Add method-level contract tests for all implemented A2A methods.
- [ ] Add lifecycle/state transition tests.
- [ ] Add SSE integration tests.
- [ ] Add `docs/development.md`, `docs/architecture.md`, `docs/TEST_PLAN.md`, `docs/PUBLISH_GUIDE.md`.

### Acceptance Criteria

- [ ] `mvn test` passes at root and module levels.
- [ ] Documentation matches actual behavior and routes.
- [ ] Test plan supports deterministic manual verification.

### Dependencies

- Issues 6, 7, 8, 9, 10, 11

## Optional: `gh` CLI Creation Workflow

If you want to create these quickly via CLI, use one issue at a time:

```bash
gh issue create --title "[M1] Refactor package structure to MCP-aligned architecture" --body-file /path/to/body.md --label enhancement --label architecture --label a2a --milestone "M1 - Foundation"
```

Use this backlog file as source text for each issue body.
