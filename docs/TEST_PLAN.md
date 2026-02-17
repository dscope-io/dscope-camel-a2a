# TEST PLAN

## Scope

This plan covers:

- JSON-RPC envelope validation and dispatch
- Contract behavior for implemented A2A methods
- Task lifecycle/state transition guarantees
- SSE task stream behavior
- Push notification config CRUD and retry stats
- Agent Card discovery and extended card retrieval
- Sample route exposure and diagnostics output

## Automated Test Commands

Run from repo root:

```bash
mvn test
```

Run per module:

```bash
mvn -pl camel-a2a-component test
mvn -pl samples/a2a-yaml-service test

# Component tests with JDBC persistence mode
mvn -pl camel-a2a-component test -Dcamel.persistence.enabled=true -Dcamel.persistence.backend=jdbc -Dcamel.persistence.jdbc.url=jdbc:derby:memory:a2a;create=true

# Component tests with Redis persistence mode (requires reachable Redis)
mvn -pl camel-a2a-component test -Dcamel.persistence.enabled=true -Dcamel.persistence.backend=redis -Dcamel.persistence.redis.uri=redis://localhost:6379
```

Expected result:

- Build succeeds
- No unit/integration test failures

## Deterministic Manual Verification

Start sample service:

```bash
cd samples/a2a-yaml-service
mvn exec:java
```

### 1. Health and Diagnostics

```bash
curl -s http://localhost:8080/health
curl -s http://localhost:8080/diagnostics
```

Expected:

- Health contains `status`
- Diagnostics contains `status`, `tasks`, `streaming`, `pushNotifications`, `supportedMethods`

### 2. Task and Lifecycle Methods

1. Call `SendMessage`, capture `taskId`.
2. Call `GetTask` with `taskId`.
3. Call `ListTasks` and verify task appears.
4. Call `CancelTask` with `taskId`.

Expected:

- JSON-RPC success envelopes (`jsonrpc=2.0`, `result`, matching `id`)
- Cancel response includes `canceled=true`
- Task state transitions are reflected in returned task status

### 3. Streaming and SSE

1. Call `SendStreamingMessage`, capture `taskId` and `subscriptionId`.
2. Call `SubscribeToTask` for same task.
3. Call `GET /a2a/sse/{taskId}` with `afterSequence=0`.

Expected:

- SSE payload includes ordered `task.status` events
- Terminal tasks emit `event: close`

### 4. Push Config Methods

1. `CreatePushNotificationConfig` with test endpoint URL
2. `GetPushNotificationConfig` using `configId`
3. `ListPushNotificationConfigs`
4. `DeletePushNotificationConfig` using `configId`

Expected:

- CRUD responses are valid JSON-RPC envelopes
- Delete returns `deleted=true` for existing config

### 5. Agent Card

```bash
curl -s http://localhost:8081/.well-known/agent-card.json
```

And via JSON-RPC:

- Call `GetExtendedAgentCard` with `includeSignature=true`

Expected:

- Discovery endpoint returns an agent card document
- Extended method returns `agentCard` and optional signature payload

### 6. Persistence Sanity (JDBC/Redis)

1. Start sample runtime with persistence enabled:
   - JDBC:
     `-Dcamel.persistence.enabled=true -Dcamel.persistence.backend=jdbc -Dcamel.persistence.jdbc.url=jdbc:derby:memory:a2a;create=true`
   - Redis:
     `-Dcamel.persistence.enabled=true -Dcamel.persistence.backend=redis -Dcamel.persistence.redis.uri=redis://localhost:6379`
2. Create task via `SendMessage`.
3. Transition state (for example `SendStreamingMessage` or `CancelTask`).
4. Restart runtime process with the same persistence settings.
5. Call `GetTask` and `GET /a2a/sse/{taskId}`.

Expected:

- task state is restored after restart
- task event stream remains available and ordered
- protocol envelopes remain unchanged (`jsonrpc=2.0`)

## Exit Criteria

- All automated commands pass.
- Manual checks above complete without protocol mismatches.
- Documentation stays aligned with routes and method behavior.
