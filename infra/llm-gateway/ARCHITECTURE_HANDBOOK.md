# LLM Gateway Architecture Handbook

## Architecture Overview
The LLM Gateway is a Spring Boot application designed to centralize access to Large Language Models.

### Core Stack
*   **Framework**: Spring Boot 3.2.5 (Virtual Threads Enabled).
*   **Language**: Kotlin 1.9.
*   **Database**: PostgreSQL (Production) / H2 (Test).
*   **Async**: Spring Integration + NATS (Events) + Postgres (State).
*   **Security**: OAuth2 Resource Server (stateless).

### Modules
1.  **Core Proxy**: `ChatController` forwards requests to `ChatModel` (Spring AI Fluent API).
2.  **Dynamic Routing**: `RuleEngine` selects models based on HOCON rules (`rules.conf`).
3.  **Observability**: `PromptLogRepository` stores logs with Full Text Search (Postgres TSVECTOR).
4.  **Async Jobs**: `JobService` manages long-running requests via Polling and Webhooks.
5.  **Interfaces**: REST, gRPC, and Vue.js Frontend.

## Reference Guide

### Phase 1: Configuration
See `docs/phase1-configuration.md` for Setup, Database, and Security details.

### Phase 2: Rules & Routing
See `docs/phase2-rules.md` for defining HOCON rules and managing Models via Frontend.

### Phase 3: Observability
See `docs/phase3-observability.md` for Logging schema, Metrics, and Dashboard.

### Phase 4: Async Patterns
See `docs/phase4-async.md` for Job API and NATS integration.

### Phase 5: gRPC
*   **Service Definition**: `src/main/proto/chat.proto`
*   **Endpoint**: Standard gRPC port (default 9090).
*   **Client Usage**: Use `grpcurl` or generated stubs.

## Deployment
*   **Docker**: `docker-compose up` starts Postgres and NATS (if configured).
*   **Build**: `./gradlew build` creates a fat JAR.
*   **Frontend**: Built automatically and embedded in the JAR.
