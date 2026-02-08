# Camel A2A Component

[![Java Version](https://img.shields.io/badge/Java-21-orange)](https://openjdk.java.net/projects/jdk/21/)
[![Apache Camel](https://img.shields.io/badge/Apache%20Camel-4.15.0-blue)](https://camel.apache.org/)
[![License](https://img.shields.io/badge/License-Apache%202.0-green.svg)](https://opensource.org/licenses/Apache-2.0)

A comprehensive Apache Camel component for Agent-to-Agent (A2A) communication using JSON-RPC 2.0 over WebSocket connections. This component enables seamless integration between AI agents, microservices, and distributed systems through standardized message passing.

## Table of Contents

- [Features](#features)
- [Architecture](#architecture)
- [Quick Start](#quick-start)
- [Installation](#installation)
- [Configuration](#configuration)
- [Usage](#usage)
- [API Reference](#api-reference)
- [Examples](#examples)
- [Building](#building)
- [Contributing](#contributing)
- [License](#license)

## Features

- **JSON-RPC 2.0 Protocol**: Full compliance with JSON-RPC 2.0 specification for structured message exchange
- **WebSocket Transport**: Real-time bidirectional communication over WebSocket connections
- **Camel Integration**: Native Apache Camel component with producer/consumer patterns
- **Intent-Based Routing**: Message routing based on method names and parameters
- **Tool Registry**: Dynamic registration and discovery of available tools/functions
- **Error Handling**: Comprehensive error response generation with fallback mechanisms
- **YAML Route Configuration**: Declarative route definition using Camel YAML DSL
- **Health Monitoring**: Built-in health check endpoints for service monitoring
- **Authentication Support**: Token-based authentication for secure communication
- **Retry Logic**: Configurable retry mechanisms for reliable message delivery

## Architecture

### Core Components

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   A2A Producer  │    │  A2A Component  │    │  A2A Consumer   │
│                 │    │                 │    │                 │
│ • Message       │◄──►│ • Endpoint      │◄──►│ • WebSocket     │
│   Creation      │    │   Management    │    │   Server        │
│ • JSON          │    │ • Configuration │    │ • Message       │
│   Serialization │    │ • Routing       │    │   Processing    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 │
                    ┌─────────────────┐
                    │  Tool Registry  │
                    │                 │
                    │ • Tool          │
                    │   Registration  │
                    │ • Discovery     │
                    │ • Metadata      │
                    └─────────────────┘
```

### Message Flow

1. **Producer**: Converts Camel exchanges into JSON-RPC 2.0 requests
2. **Transport**: WebSocket connection handles bidirectional communication
3. **Consumer**: Receives messages and routes them through Camel processors
4. **Registry**: Maintains available tools and their capabilities

## Quick Start

### Prerequisites

- Java 21 or higher
- Maven 3.6+
- Apache Camel 4.15.0

### Running the Demo

```bash
# Clone the repository
git clone <repository-url>
cd camel-a2a

# Build and install locally (all modules)
mvn clean install

# Run the sample application (basic sample by default)
cd samples/a2a-yaml-service
mvn exec:java

# Or run a specific sample
mvn exec:java -Dexec.args="standalone"
```

The application will start:
- Health check endpoint: `http://localhost:8080/health`
- A2A WebSocket server: `ws://localhost:8081/a2a` (basic) or `ws://localhost:8082/a2a` (standalone)

### Testing the Service

```bash
# Health check
curl http://localhost:8080/health
# Response: {"status":"UP"}

# WebSocket connection (using wscat or similar)
wscat -c ws://localhost:8081/a2a
# Send: {"jsonrpc":"2.0","method":"test","params":{},"id":"1"}
```

## Sample Organization

The samples are organized into subdirectories for better maintainability:

```
samples/
├── pom.xml                                # Samples aggregator
└── a2a-yaml-service/
    ├── pom.xml                            # YAML service sample module
    └── src/
        ├── main/java/io/dscope/camel/a2a/samples/
        │   ├── Main.java                  # Main entry point for all samples
        │   ├── basic/                     # Basic A2A platform sample
        │   │   └── Runner.java            # Basic sample implementation
        │   └── standalone/                # Standalone minimal sample
        │       └── Runner.java            # Standalone sample implementation
        └── main/resources/
            ├── basic/routes/
            │   └── a2a-platform.yaml
            └── standalone/routes/
                └── standalone-sample.yaml
```

### Available Samples

- **basic** (default): Basic A2A platform with health check and WebSocket endpoints
- **standalone**: Minimal A2A setup with custom configuration and timestamp responses

### Running Specific Samples

```bash
cd samples/a2a-yaml-service

# Run basic sample (default)
mvn exec:java

# Run standalone sample
mvn exec:java -Dexec.args="standalone"

# Run directly without main dispatcher
mvn exec:java -Dexec.mainClass="io.dscope.camel.a2a.samples.basic.Runner"
mvn exec:java -Dexec.mainClass="io.dscope.camel.a2a.samples.standalone.Runner"
```

## Installation

### Maven Dependency

Add the following to your `pom.xml`:

```xml
<dependency>
    <groupId>io.dscope.camel</groupId>
    <artifactId>camel-a2a-component</artifactId>
    <version>0.5.0-SNAPSHOT</version>
</dependency>
```

### Manual Installation

```bash
# Build and install locally (all modules)
mvn clean install

# Or build fat JAR for component
mvn clean package -pl camel-a2a-component
```

## Configuration

### URI Format

```
a2a:agent[?options]
```

### URI Parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `agent` | String | - | **Required.** Target agent identifier |
| `remoteUrl` | String | `ws://localhost:8081/a2a` | Remote A2A server URL |
| `serverUrl` | String | `ws://0.0.0.0:8081/a2a` | Local server URL for consumers |
| `protocolVersion` | String | `a2a/2025-06-18` | Protocol version identifier |
| `sendToAll` | Boolean | `false` | Send to all connected agents |
| `authToken` | String | - | Authentication token |
| `retryCount` | Integer | `3` | Number of retry attempts |
| `retryDelayMs` | Long | `500` | Delay between retries (ms) |

### Example URIs

```java
// Basic producer
from("direct:input").to("a2a:myAgent");

// Consumer with custom server
from("a2a:myAgent?serverUrl=ws://0.0.0.0:9090/a2a")
    .process(new MyProcessor());

// Producer with authentication
from("direct:secure").to("a2a:secureAgent?authToken=abc123&retryCount=5");
```

## Usage

### Basic Producer

```java
from("direct:sendMessage")
    .to("a2a:myAgent")
    .log("Sent message: ${body}");
```

### Consumer with Processing

```java
from("a2a:myAgent")
    .process(exchange -> {
        // Process incoming A2A message
        String body = exchange.getIn().getBody(String.class);
        // ... business logic ...
        exchange.getMessage().setBody("Processed: " + body);
    });
```

### Tool Registration

```java
@BindToRegistry("a2aToolRegistry")
public class MyToolRegistry {
    private final A2AToolRegistry registry = new A2AToolRegistry();

    @PostConstruct
    public void init() {
        registry.register("echo", "direct:echo", "Echo service");
        registry.register("calculator", "direct:calc", "Calculator service");
    }
}
```

## API Reference

### A2AMessage

JSON-RPC 2.0 message container.

```java
// Create a request
A2AMessage request = A2AMessage.request("methodName", parameters);

// Access fields
String id = request.getId();
String method = request.getMethod();
Object params = request.getParams();
```

### A2AJsonCodec

JSON serialization/deserialization utility.

```java
A2AJsonCodec codec = new A2AJsonCodec();

// Serialize
String json = codec.serialize(myObject);

// Deserialize
MyClass obj = codec.deserialize(json, MyClass.class);
```

### JsonRpcError

Error response generation.

```java
// Create error response
String errorJson = JsonRpcError.envelope("requestId", 123, "Error message");
```

### A2AToolRegistry

Tool registration and discovery.

```java
A2AToolRegistry registry = new A2AToolRegistry();

// Register tool
registry.register("toolName", "direct:route", "Tool description");

// List tools
List<Map<String, Object>> tools = registry.list();
```

## Examples

### Complete Route Configuration

```yaml
# routes/a2a-platform.yaml
- route:
    id: health-check
    from: "undertow:http://0.0.0.0:8080/health?httpMethodRestrict=GET"
    steps:
      - setBody:
          constant: '{"status":"UP"}'

- route:
    id: a2a-handler
    from: "a2a://agent?serverUrl=ws://0.0.0.0:8081/a2a"
    steps:
      - log: "Received A2A message: ${body}"
      - process:
          ref: intentRouter
      - to: "a2a://agent"

- route:
    id: echo-tool
    from: "direct:echo"
    steps:
      - log: "Echo request: ${body}"
      - setBody:
          simple: '{"result": "${body}", "timestamp": "${date:now}"}'
```

### Java Route Builder

```java
public class A2ARoutes extends RouteBuilder {
    @Override
    public void configure() {
        // Health endpoint
        from("undertow:http://0.0.0.0:8080/health")
            .setBody(constant("{\"status\":\"UP\"}"));

        // A2A consumer
        from("a2a://myAgent")
            .process(new IntentRouterProcessor())
            .to("a2a://myAgent");

        // Tool routes
        from("direct:calculator")
            .process(exchange -> {
                // Calculator logic
                String expression = exchange.getIn().getBody(String.class);
                double result = evaluate(expression);
                exchange.getMessage().setBody("{\"result\": " + result + "}");
            });
    }
}
```

### Spring Boot Integration

```java
@SpringBootApplication
public class A2AApplication {
    public static void main(String[] args) {
        SpringApplication.run(A2AApplication.class, args);
    }

    @Bean
    public RouteBuilder a2aRoutes() {
        return new RouteBuilder() {
            @Override
            public void configure() {
                from("a2a://springAgent")
                    .log("Spring A2A: ${body}")
                    .to("mock:result");
            }
        };
    }
}
```

## Building

### Prerequisites

- Java 21 JDK
- Maven 3.6+

### Build Commands

```bash
# Build all modules (component + samples)
mvn clean compile

# Build only the component
mvn clean compile -pl camel-a2a-component

# Build only samples
mvn clean compile -pl samples

# Run tests for all modules
mvn test

# Create JAR with dependencies for component
mvn package -pl camel-a2a-component

# Create JAR for samples
mvn package -pl samples

# Install all to local repository
mvn install

# Generate documentation
mvn javadoc:javadoc -pl camel-a2a-component
```

### Running Samples

After building, you can run the sample application:

```bash
# Run samples (requires component to be installed first)
cd samples/a2a-yaml-service
mvn exec:java

# Or run directly with full classpath
mvn exec:java -pl samples/a2a-yaml-service
```

The sample application starts:
- Health check endpoint: http://localhost:8080/health
- A2A WebSocket server: ws://localhost:8081/a2a
- Example tool route: direct:echo

### Docker Build

```dockerfile
FROM openjdk:21-jdk-slim
COPY camel-a2a-component/target/camel-a2a.jar app.jar
EXPOSE 8080 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
```

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Setup

```bash
# Clone and setup
git clone <repository-url>
cd camel-a2a

# Build all modules
mvn clean compile

# Run tests for all modules
mvn test

# Run the sample application
cd samples/a2a-yaml-service
mvn exec:java

# Run with debugging (from sample module directory)
mvn exec:java -Ddebug
```

### Code Style

- Follow Java 21 best practices
- Use meaningful variable and method names
- Add JavaDoc comments for public APIs
- Maintain test coverage above 80%

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## Support

For questions and support:
- Create an issue on GitHub
- Check the [Apache Camel documentation](https://camel.apache.org/)
- Review JSON-RPC 2.0 [specification](https://www.jsonrpc.org/specification)

---

**Generated on:** October 17, 2025
**Version:** 0.5.0-SNAPSHOT
**Camel Version:** 4.15.0
