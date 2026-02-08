# Apache Camel A2A Compatibility Plan

Last updated: 2026-02-07

## Goal

Make `camel-a2a-component` fully compatible with the latest A2A protocol, while following the same architecture and delivery patterns used in `CamelMcpComponent`.

## Compliance Baseline

1. Use the latest A2A protocol specification as source of truth.
2. Implement JSON-RPC methods required by the latest spec, including:
   - `SendMessage`
   - `SendStreamingMessage`
   - `GetTask`
   - `ListTasks`
   - `CancelTask`
   - `SubscribeToTask`
   - push notification config methods
   - `GetExtendedAgentCard`
3. Expose discovery endpoint:
   - `/.well-known/agent-card.json`
4. Support HTTP + SSE as primary compliance path, with WebSocket as optional extension.

## Current Gaps (Project Assessment)

1. `camel-a2a-component/src/main/java/io/dscope/camel/a2a/A2AProducer.java`
   - Hardcodes `intent/execute`, which is not aligned with current A2A core method set.
2. `camel-a2a-component/src/main/java/io/dscope/camel/a2a/A2AMessage.java`
   - Request-only structure; missing protocol-complete response, task, and artifact models.
3. `camel-a2a-component/src/main/java/io/dscope/camel/a2a/A2AConsumer.java`
   - Adds a simple echo-style route, not a protocol dispatcher.
4. `samples/a2a-yaml-service/src/main/resources/basic/routes/a2a-platform.yaml`
   - No A2A method routing pipeline for production-compatible behavior.
5. Project architecture
   - Missing MCP-style processor/catalog/service modularization and bootstrapping support.

## Architecture Direction (Mirror MCP Patterns)

Adopt structure similar to `CamelMcpComponent`:

- `io.dscope.camel.a2a.model`
- `io.dscope.camel.a2a.processor`
- `io.dscope.camel.a2a.catalog`
- `io.dscope.camel.a2a.service`
- `io.dscope.camel.a2a.config`
- `A2AComponentApplicationSupport` for consistent bean binding and sample bootstrapping

## Implementation Phases

## Phase 1: Foundation Refactor

1. Create package structure (`model`, `processor`, `catalog`, `service`, `config`).
2. Add JSON-RPC envelope parser/validator processor.
3. Add standard JSON-RPC error response processor.
4. Introduce `A2AComponentApplicationSupport` (MCP-style startup and bean wiring).
5. Add base request/response abstractions and shared exchange property constants.

## Phase 2: Core Protocol Methods

1. Implement typed models for:
   - `Task`, `TaskStatus`, `Message`, `Part`, `Artifact`, `AgentCard`
2. Implement method processors for:
   - `SendMessage`, `GetTask`, `ListTasks`, `CancelTask`
3. Add route-level method dispatch pattern (choice/router equivalent to MCP services).
4. Add strict method/parameter validation with protocol-aligned JSON-RPC error codes.

## Phase 3: Stateful Task Engine

1. Implement `A2ATaskRegistry` (or `A2ATaskService`) for lifecycle management.
2. Enforce legal state transitions and terminal state guarantees.
3. Add task history and metadata for auditing.
4. Add idempotency behavior for repeated request identifiers/message identifiers.

## Phase 4: Streaming and Async

1. Implement `SendStreamingMessage` and `SubscribeToTask` over SSE.
2. Add event publisher for task state and artifact updates.
3. Add timeout/backpressure handling and safe subscriber cleanup.
4. Add terminal-state closure semantics for subscriptions.

## Phase 5: Discovery and Security Capabilities

1. Implement `/.well-known/agent-card.json`.
2. Implement `GetExtendedAgentCard`.
3. Add card capability declarations and auth scheme metadata.
4. Add extension points for card signing/verification (JWS) and mTLS/JWT policy checks.

## Phase 6: Push Notification Configuration APIs

1. Implement create/get/list/delete push notification config methods.
2. Add notifier service abstraction and sample webhook notifier.
3. Add retry and failure handling behavior for push delivery.

## Phase 7: Samples, Tests, and Documentation

1. Build production-style sample service routes (HTTP + SSE) following MCP sample quality.
2. Add protocol contract tests covering all implemented methods.
3. Add lifecycle tests for task transitions and cancellation paths.
4. Add SSE integration tests for stream correctness and completion.
5. Add docs:
   - `docs/development.md`
   - `docs/architecture.md`
   - `docs/TEST_PLAN.md`
   - `docs/PUBLISH_GUIDE.md`

## PR Breakdown (Recommended)

1. PR1: Foundation refactor + model skeleton + envelope/error processors.
2. PR2: Core methods (`SendMessage`, `GetTask`, `ListTasks`, `CancelTask`) + task service + HTTP sample route.
3. PR3: Streaming/subscription + push config APIs.
4. PR4: Agent card + extended card + security hooks + full docs/tests hardening.

## Quality Gates Per PR

1. All unit tests pass (`mvn test` at module and root levels).
2. New code has method-level validation and JSON-RPC error mapping tests.
3. Sample routes are executable and manually testable with documented curl examples.
4. No regression in existing component API behavior unless explicitly versioned.

## Notes

This plan intentionally prioritizes protocol correctness and MCP-aligned internal architecture first, then transport extensions and hardening.
