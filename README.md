# Camel A2A Component

[![Java Version](https://img.shields.io/badge/Java-21-orange)](https://openjdk.java.net/projects/jdk/21/)
[![Apache Camel](https://img.shields.io/badge/Apache%20Camel-4.15.0-blue)](https://camel.apache.org/)
[![License](https://img.shields.io/badge/License-Apache%202.0-green.svg)](https://opensource.org/licenses/Apache-2.0)

Apache Camel component and sample runtime for Agent-to-Agent (A2A) protocol workflows.

This repository provides:

- A reusable Camel component (`a2a:`) for producer/consumer integration.
- A protocol service runtime with JSON-RPC 2.0, task lifecycle methods, SSE streaming, push notification config APIs, and agent card discovery.
- Optional persistence-backed task/event services via shared `camel-persistence` backends.
- A sample YAML-based service that exposes a practical HTTP surface for local testing and integration.

## Table of Contents

- [Project Structure](#project-structure)
- [What You Get](#what-you-get)
- [Supported Methods](#supported-methods)
- [Quick Start](#quick-start)
- [Runtime Endpoints](#runtime-endpoints)
- [Manual Smoke Test](#manual-smoke-test)
- [Camel Component URI Options](#camel-component-uri-options)
- [Build and Test](#build-and-test)
- [Documentation](#documentation)
- [License](#license)

## Project Structure

```text
camel-a2a/
|- pom.xml                                  # Root aggregator
|- camel-a2a-component/                     # Core component + protocol implementation
|  |- src/main/java/io/dscope/camel/a2a/
|  |- src/main/java/io/dscope/camel/a2a/config/
|  |- src/main/java/io/dscope/camel/a2a/processor/
|  |- src/main/java/io/dscope/camel/a2a/service/
|  |- src/main/java/io/dscope/camel/a2a/model/
|  `- src/main/java/io/dscope/camel/a2a/catalog/
|- samples/
|  `- a2a-yaml-service/                     # Runnable sample runtime
|     |- src/main/java/io/dscope/camel/a2a/samples/
|     `- src/main/resources/
|        |- basic/routes/a2a-platform.camel.yaml
|        `- standalone/routes/standalone-sample.camel.yaml
`- docs/
   |- architecture.md
   |- development.md
   |- TEST_PLAN.md
   `- PUBLISH_GUIDE.md
```

## What You Get

### Core Protocol Layer

- JSON-RPC envelope parsing and validation via `A2AJsonRpcEnvelopeProcessor`.
- Method dispatch via `A2AMethodDispatchProcessor`.
- JSON-RPC error normalization via `A2AErrorProcessor`.
- Canonical method set in `A2AProtocolMethods`.

### Task and Streaming Layer

- In-memory task lifecycle state machine via `InMemoryA2ATaskService`.
- Task event sequencing and subscriptions via `InMemoryTaskEventService`.
- SSE rendering via `A2ATaskSseProcessor`.

### Push Notification Layer

- CRUD methods for push configurations.
- Retry and backoff controls.
- Delivery stats surfaced in diagnostics.

### Discovery Layer

- Agent card discovery endpoint (`/.well-known/agent-card.json`).
- Extended card retrieval method (`GetExtendedAgentCard`).

### Runtime Bootstrap

- `A2AComponentApplicationSupport` binds all default beans and method processors.
- YAML route include validation and simple sample bootstrapping for both basic and standalone modes.

## Supported Methods

`POST /a2a/rpc` supports the following protocol methods:

| Method | Required Params | Notes |
|---|---|---|
| `SendMessage` | `message` | Creates task and returns `task` |
| `SendStreamingMessage` | `message` | Creates task, drives streaming state updates, returns `subscriptionId` and `streamUrl` |
| `GetTask` | `taskId` | Returns task snapshot |
| `ListTasks` | none | Optional `limit`, `state`, `cursor` |
| `CancelTask` | `taskId` | Optional `reason` |
| `SubscribeToTask` | `taskId` | Optional `afterSequence`, `limit` |
| `CreatePushNotificationConfig` | `endpointUrl` | Optional `taskId`, retry/backoff, headers, metadata |
| `GetPushNotificationConfig` | `configId` | Fetches config by id |
| `ListPushNotificationConfigs` | none | Optional `taskId`, `limit` |
| `DeletePushNotificationConfig` | `configId` | Returns deletion status |
| `GetExtendedAgentCard` | none | Optional `includeSignature` (default `true`) |
| `intent/execute` | implementation-dependent | Legacy compatibility method constant |

## Quick Start

### Prerequisites

- Java 21+
- Maven 3.6+

### Build and Test

```bash
mvn clean test
```

### Run Sample Runtime

```bash
cd samples/a2a-yaml-service

# Default sample (Redis persistence defaults enabled)
mvn exec:java

# Standalone sample
mvn exec:java -Dexec.args="standalone"

# Override sample service port
A2A_SAMPLE_PORT=8090 mvn exec:java
```

By default, sample runners set:

- `camel.persistence.enabled=true`
- `camel.persistence.backend=redis`
- `camel.persistence.redis.uri` from `REDIS_URI` env var when present, otherwise `redis://localhost:6379`
- `a2a.sample.port` from `A2A_SAMPLE_PORT` env var when present, otherwise `8080`

### Persistence Quickstart

JDBC mode (embedded Derby):

```bash
cd samples/a2a-yaml-service
mvn exec:java -Dcamel.persistence.enabled=true -Dcamel.persistence.backend=jdbc -Dcamel.persistence.jdbc.url=jdbc:derby:memory:a2a;create=true
```

Redis mode:

Start a local Redis first (if you do not already have one running):

```bash
docker run --name a2a-redis -p 6379:6379 -d redis:7-alpine
```

Then run the sample with Redis persistence:

```bash
cd samples/a2a-yaml-service
mvn exec:java -Dcamel.persistence.enabled=true -Dcamel.persistence.backend=redis -Dcamel.persistence.redis.uri=redis://localhost:6379
```

## Runtime Endpoints

Both sample modes expose the same protocol surface:

| Method | URL | Purpose |
|---|---|---|
| `GET` | `http://localhost:8080/health` | Health/liveness |
| `GET` | `http://localhost:8080/diagnostics` | Runtime counters and supported methods |
| `POST` | `http://localhost:8080/a2a/rpc` | JSON-RPC protocol entrypoint |
| `GET` | `http://localhost:8080/a2a/sse/{taskId}` | Task event stream |
| `GET` | `http://localhost:8080/.well-known/agent-card.json` | Agent card discovery |

## Manual Smoke Test

Run sample first, then execute:

```bash
# Health + diagnostics
curl -s http://localhost:8080/health
curl -s http://localhost:8080/diagnostics

# SendMessage -> capture taskId
SEND_RESPONSE=$(curl -s http://localhost:8080/a2a/rpc \
  -H 'Content-Type: application/json' \
  -d '{"jsonrpc":"2.0","method":"SendMessage","params":{"message":{"messageId":"m1","role":"user","parts":[{"partId":"p1","type":"text","text":"hello"}]},"metadata":{"source":"curl"}},"id":"1"}')

echo "$SEND_RESPONSE"
TASK_ID=$(echo "$SEND_RESPONSE" | sed -n 's/.*"taskId":"\([^"]*\)".*/\1/p')

# GetTask + ListTasks
curl -s http://localhost:8080/a2a/rpc \
  -H 'Content-Type: application/json' \
  -d "{\"jsonrpc\":\"2.0\",\"method\":\"GetTask\",\"params\":{\"taskId\":\"$TASK_ID\"},\"id\":\"2\"}"

curl -s http://localhost:8080/a2a/rpc \
  -H 'Content-Type: application/json' \
  -d '{"jsonrpc":"2.0","method":"ListTasks","params":{"limit":10},"id":"3"}'

# Subscribe + SSE
curl -s http://localhost:8080/a2a/rpc \
  -H 'Content-Type: application/json' \
  -d "{\"jsonrpc\":\"2.0\",\"method\":\"SubscribeToTask\",\"params\":{\"taskId\":\"$TASK_ID\",\"afterSequence\":0,\"limit\":20},\"id\":\"4\"}"

curl -N "http://localhost:8080/a2a/sse/$TASK_ID?afterSequence=0&limit=100"

# Cancel task
curl -s http://localhost:8080/a2a/rpc \
  -H 'Content-Type: application/json' \
  -d "{\"jsonrpc\":\"2.0\",\"method\":\"CancelTask\",\"params\":{\"taskId\":\"$TASK_ID\",\"reason\":\"manual stop\"},\"id\":\"5\"}"

# Agent card endpoints
curl -s http://localhost:8080/.well-known/agent-card.json
curl -s http://localhost:8080/a2a/rpc \
  -H 'Content-Type: application/json' \
  -d '{"jsonrpc":"2.0","method":"GetExtendedAgentCard","params":{"includeSignature":true},"id":"6"}'
```

## Camel Component URI Options

The reusable Camel endpoint URI format:

```text
a2a:agent[?options]
```

| Parameter | Type | Default | Description |
|---|---|---|---|
| `agent` | String | - | Endpoint agent identifier |
| `remoteUrl` | String | `ws://localhost:8081/a2a` | Remote target URL |
| `serverUrl` | String | `ws://0.0.0.0:8081/a2a` | Local consumer bind URL |
| `protocolVersion` | String | `a2a/2025-06-18` | Protocol version marker |
| `sendToAll` | Boolean | `false` | Broadcast behavior |
| `authToken` | String | - | Optional token |
| `retryCount` | Integer | `3` | Send retry count |
| `retryDelayMs` | Long | `500` | Retry backoff ms |

## Build and Test

From root:

```bash
# Full build + test
mvn clean test

# Component only
mvn -pl camel-a2a-component test

# Sample module only
mvn -pl samples/a2a-yaml-service test

# Sample Redis persistence default wiring test only
mvn -pl samples/a2a-yaml-service -Dtest=SamplePersistenceDefaultsTest test

# Persistence-backed component tests
mvn -pl camel-a2a-component test -Dcamel.persistence.enabled=true -Dcamel.persistence.backend=jdbc -Dcamel.persistence.jdbc.url=jdbc:derby:memory:a2a;create=true
```

Package artifacts:

```bash
mvn package
mvn package -pl camel-a2a-component
mvn package -pl samples/a2a-yaml-service
```

## Persistence Configuration

Persistence is disabled by default. Enable it with:

```bash
-Dcamel.persistence.enabled=true
-Dcamel.persistence.backend=redis|jdbc|ic4j
```

Common persistence properties:

- `camel.persistence.snapshot-every-events` (default `25`)
- `camel.persistence.max-replay-events` (default `500`)

Redis backend properties:

- `camel.persistence.redis.uri` (default `redis://localhost:6379`)
- `camel.persistence.redis.key-prefix` (default `camel:state`)

JDBC backend properties:

- `camel.persistence.jdbc.url` (example: `jdbc:derby:memory:a2a;create=true`)
- `camel.persistence.jdbc.user`
- `camel.persistence.jdbc.password`

## Documentation

- Detailed architecture: `/Users/roman/Projects/DScope/CamelA2AComponent/docs/architecture.md`
- Developer guide: `/Users/roman/Projects/DScope/CamelA2AComponent/docs/development.md`
- Test plan: `/Users/roman/Projects/DScope/CamelA2AComponent/docs/TEST_PLAN.md`
- Publish guide: `/Users/roman/Projects/DScope/CamelA2AComponent/docs/PUBLISH_GUIDE.md`

## License

Apache License 2.0. See `LICENSE`.
