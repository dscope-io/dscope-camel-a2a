# Developer Guide

## 1. Purpose

This guide is for contributors working on the Camel A2A component and its sample protocol runtime.

It covers:

- local setup
- common workflows
- codebase conventions
- testing strategy
- extension and debugging patterns

## 2. Prerequisites

- Java 21+
- Maven 3.6+
- `curl` for local endpoint checks

Optional:

- `jq` for nicer JSON output

## 3. First-Time Setup

From repository root:

```bash
mvn clean test
```

This compiles all modules and runs tests.

## 4. Repository Layout

- `/Users/roman/Projects/DScope/CamelA2AComponent/pom.xml`
  - root aggregator
- `/Users/roman/Projects/DScope/CamelA2AComponent/camel-a2a-component`
  - core component, models, processors, services
- `/Users/roman/Projects/DScope/CamelA2AComponent/samples/a2a-yaml-service`
  - runnable YAML-based sample runtime
- `/Users/roman/Projects/DScope/CamelA2AComponent/docs`
  - architecture, test, release, and development docs

## 5. Run Modes

### 5.1 Run Sample Runtime

```bash
cd /Users/roman/Projects/DScope/CamelA2AComponent/samples/a2a-yaml-service
mvn exec:java
```

Standalone mode:

```bash
cd /Users/roman/Projects/DScope/CamelA2AComponent/samples/a2a-yaml-service
mvn exec:java -Dexec.args="standalone"
```

### 5.2 Endpoints to Verify

- `GET http://localhost:8080/health`
- `GET http://localhost:8080/diagnostics`
- `POST http://localhost:8081/a2a/rpc`
- `GET http://localhost:8081/a2a/sse/{taskId}`
- `GET http://localhost:8081/.well-known/agent-card.json`

## 6. Typical Development Workflow

1. Pick one scoped change.
2. Update implementation in `camel-a2a-component`.
3. Add/adjust targeted tests in corresponding package.
4. Run module tests.
5. Run root tests.
6. Smoke test changed endpoints against sample runtime.
7. Update docs when behavior changes.

Suggested command sequence:

```bash
# Fast feedback
mvn -pl camel-a2a-component test

# Full verification
mvn test
```

## 7. Where to Implement What

### 7.1 New JSON-RPC Method

1. Add method constant in `A2AProtocolMethods`.
2. Add DTO request/response classes under `model/dto` if needed.
3. Implement processor under `processor/`.
4. Register processor in method map in `A2AComponentApplicationSupport`.
5. Add tests:
   - dispatch contract test
   - parameter validation test
   - error mapping assertions
6. Update README method table.

### 7.2 New Diagnostics Field

1. Extend `A2ADiagnosticsProcessor`.
2. Add/adjust test in `A2ADiagnosticsProcessorTest`.
3. Document field in README and/or test plan if relevant.

### 7.3 Task Lifecycle Changes

1. Update transition matrix in `InMemoryA2ATaskService`.
2. Add transition-path tests in `InMemoryA2ATaskServiceTest`.
3. Validate SSE side effects with `A2ATaskSseProcessorTest`.

### 7.4 Push Delivery Behavior Changes

1. Update `InMemoryPushNotificationConfigService`.
2. Adjust notifier/observer behavior as needed.
3. Add or update tests in `InMemoryPushNotificationConfigServiceTest`.
4. Confirm diagnostics counters still match behavior.

## 8. Testing Layers

### 8.1 Unit + Contract Tests

Most behavior is validated by tests in:

- `/Users/roman/Projects/DScope/CamelA2AComponent/camel-a2a-component/src/test/java/io/dscope/camel/a2a/processor`
- `/Users/roman/Projects/DScope/CamelA2AComponent/camel-a2a-component/src/test/java/io/dscope/camel/a2a/service`

Coverage includes:

- envelope validation
- method dispatch
- method contract success/failure paths
- lifecycle transition legality
- SSE stream generation
- push config CRUD and retry stats

### 8.2 Manual Runtime Checks

Use the curl sequence in `/Users/roman/Projects/DScope/CamelA2AComponent/README.md` for end-to-end sanity.

## 9. Error Handling Expectations

`A2AErrorProcessor` maps exceptions to JSON-RPC codes:

- `-32601` method not found
- `-32602` invalid params
- `-32600` invalid request
- `-32603` internal error

When adding new exceptions, ensure they map appropriately and include actionable messages.

## 10. Coding Conventions

- Keep processors small and method-focused.
- Validate params early and throw `A2AInvalidParamsException` for contract violations.
- Keep state transitions centralized in task service.
- Prefer immutable copies at service boundaries where practical.
- Preserve backward-compatible wrappers in `io.dscope.camel.a2a` when moving classes.

## 11. Debugging Tips

- If RPC requests fail, inspect envelope parsing first (`A2AJsonRpcEnvelopeProcessor`).
- If methods are rejected, confirm inclusion in `A2AProtocolMethods.CORE_METHODS` and method map binding.
- If SSE seems empty, verify task event publication and `afterSequence` values.
- If push notifications are not firing, verify config enabled state, task filter, and notifier attempts.

## 12. Release-Oriented Checks

Before release work, run:

```bash
mvn test
mvn -pl camel-a2a-component test
mvn -pl samples/a2a-yaml-service test
```

Then follow `/Users/roman/Projects/DScope/CamelA2AComponent/docs/PUBLISH_GUIDE.md`.

## 13. Related Docs

- Architecture: `/Users/roman/Projects/DScope/CamelA2AComponent/docs/architecture.md`
- Test plan: `/Users/roman/Projects/DScope/CamelA2AComponent/docs/TEST_PLAN.md`
- Publish guide: `/Users/roman/Projects/DScope/CamelA2AComponent/docs/PUBLISH_GUIDE.md`
