# Agent Instructions & Repository Context

# 1. Repository Overview
This is a polyglot monorepo containing microservices and frontends.
**Do not infer build steps.** Follow the strict conventions below.

- **Root Build Tool:** Gradle 9 (Kotlin DSL) + `just` (Command Runner)
- **Primary Languages:** Kotlin (JVM 21), Python (3.11+), TypeScript (Vue 3)
- **Infrastructure:** Kubernetes (K3s Local, Azure AKS Prod), ArgoCD

## 2. Directory Structure
- `agents/<service-name>`: Application source code for agentic services.
- `frontends/<service-name>`: Application source code for frontends.
- `infra/<service-name>`: Application source code for technical services.
- `services/<service-name>`: Application source code for business logic services.
- `tools/<service-name>`: Application source code for agentic tools and MCP services.
- `shared/libs/<lib-name>`: Shared libraries.
- `shared/proto`: **Source of Truth** for APIs. Contains `.proto` files.
- `gradle-build`: Gradle convention plugins. **Modify these instead of individual build.gradle.kts files.**
- `gradle/libs.versions.toml`: Central Version Catalog. **All dependency versions must be defined here.**
- `generated`: folder for generated code.

## 3. Technology Stack & Rules

### A. Kotlin Services (Spring Boot & Ktor)
- **Build Tool:** Gradle
- **Containerization:** **Jib** (Google Cloud Tools).
    - ❌ **NO Dockerfiles** for Kotlin apps.
    - ✅ Use `just deploy-kt <service>` to build and load into K3s.
- **Testing:** Kotest + Testcontainers (Wiremock).
- **Plugins:** Apply `id("my.kotlin-spring")` or `id("my.kotlin-ktor")`.

### B. Python Services (FastAPI)
- **Build Tool:** `uv` (by Astral).
- **Containerization:** Standard `Dockerfile`.
- **Dependency Mgmt:** `pyproject.toml` + `uv.lock`.
- **Proto Consumption:** **Strict Rule.** Python services consume protos as a local dependency from `libs/shared-proto/build/python-package`.
    - ✅ Use `just proto-py` to regenerate before syncing.

### C. Frontend (Vue + Vite)
- **Build Tool:** npm + vite.
- **Containerization:** Standard `Dockerfile` (Nginx).
- **Proto Consumption:** Consumes protos as a local file dependency from `libs/shared-proto/build/js-package`.

## 4. Development Workflow (The "Just" Commander)
Always suggest `just` commands for interactions.

| Action | Command | Context |
| :--- | :--- | :--- |
| **Initialize Repo** | `just init` | Installs Gradle, uv, npm deps & compiles protos. |
| **Build Kotlin** | `just build-kt <service>` | Compiles JAR. |
| **Build Python** | `just sync-py` | Syncs `uv` environments. |
| **Deploy Local** | `just deploy-<type> <service>` | Builds image & loads directly into K3s (`nerdctl load`). |
| **Debug** | `just debug-tunnel` | Ports forwards K3s services (DB, Wiremock) to localhost. |
| **Regenerate Protos**| `just proto-all` | Recompiles `.proto` files for KT, PY, and JS. |

## 5. Protocol Buffers Strategy
- **Versioning:** Folder based: `src/main/proto/com/example/payment/v1/payment.proto`.
- **Modification:** 1. Edit `.proto` file.
    2. Run `just proto-all`.
    3. Kotlin: Imports are immediately available.
    4. Python: `uv sync` is triggered automatically by `just`.
    5. JS: `npm install` is triggered automatically.

## 6. CI/CD & Versioning
- **Versioning:** Strict Semantic Versioning via Git Tags.
- **Tag Format:** `<service-directory-name>/v<major>.<minor>.<patch>`
    - Example: `payment-service-spring/v1.0.2`
- **CI Logic:** The pipeline automatically detects the project type (Jib vs Docker) based on Gradle plugins. Do not hardcode service lists in GitHub Actions.

## 7. Dependency Management
- **Never** hardcode versions in `build.gradle.kts`.
- **Always** add version to `[versions]` and library to `[libraries]` in `gradle/libs.versions.toml`.
- **Usage:** `implementation(libs.my.library)`

## 8. Common Pitfalls to Avoid
- **Python Imports:** Do not try to import protos from `src/`. They live in the generated `libs/shared-proto` package.
- **Local Images:** When writing K8s manifests for local dev, always use `imagePullPolicy: Never`.
- **Gradle:** Do not use `subprojects {}` or `allprojects {}` in the root build file. Use Convention Plugins in `build-logic`.

# Technology Stack Instructions

## Frontend

### ✅ Do
- use Typescript for logic, use plain JS for embedded scripts
- wherever possible, use Kotlin/JS for frontend
- use Vue for frontend
- keep components small
- keep diffs small and focused
- always split into CSS and HTML files, never inline CSS or HTML into code
- unless trivial, separate JS script from HTML template

### ❌ Don't
- don't hardcode colors
- don't use `<div>` if a component already exists
- don't bring in new heavy dependencies without approval

## Backend
We have three options for building backend:
1. Kotlin + Ktor
2. Kotlin + Spring Boot
3. Python

### Kotlin + Ktor Tech Stack
- Kotlin
- Ktor, both server and client
- kotlinx.serialization or Ktor JSON serialization
- JetBrains Exposed for SQL DSL; use the DSL, not the ORM
- HOCON (Human-Optimized Config Object Notation) (com.typesafe.config) for configuration (see `application.conf`)
- Clikt for command line parsing and CLI applications


### Kotlin + Spring Boot Tech Stack
- Kotlin
- Spring Boot 3.5
- Jackson serialization
- Spring Data JDBC Templates for SQL access
- Spring Boot CLI for CLI applications
- Spring Cloud to communicate with Azure (and other clouds)
- Spring MVC for REST API
- Spring Security for authentication and authorization

### ✅ Kotlin: Do 
- use Kotlin wherever possible for all backend logic
- use Kotlin coroutines (suspend fun) for async logic
- create class-level loggers and log often with DEBUG level
- when asked to serialize / deserialize data with multi-type fields, use sealed Interface and internal classes as specified below in the "Serialization Example" section 
- use builders with fluent logic wherever appropriate
- use "companion object" factories for constructors and DSLs
- strictly prefer smaller classes and short methods
- detach logic from presentation / communication (e.g. always separate "handlers" from "routes")
- create both unit and component tests; for multiple components always test one component at a time, mocking (with Wiremock) the other ones
- for larger features use TDD: first design the tests, get them approved, then implement the feature
- use Iterable and iterators when possible, try to avoid specific implementations unless necessary
- use mutable collections when needed; try to avoid copying immutable ones
- comment the code extensively
- use JSON logging; we are using Loki for logs
- use OAuth2 for authentication and authorization; we use Keycloak internally and the on-behalf-of flow
- use gRPC for internal communication
- use Kotest for testing, StringSpec variant
- use Wiremock for mocking external services in integration tests
- use ANTLR4 for parsing + grammars
- use Gradle 9 for build automation
- use slf4j for logging
- use Kover for code coverage
- use Flyway for database migrations
- use OpenTelemetry collection, Prometheus for metrics, Loki for logs, Tempo for traces. Grafana Alloy as the collector
- use SQLite for dev databases; PostgreSQL for production
- use OpenAPI for REST API documentation

### ❌ Kotlin: Don't
- don't install dependencies without approval, apart from the pre-approved ones in the "Tech Stack" section
- don't create large files; multiple classes in a single file are fine, but keep the files below 300 lines, unless necessary
- don't use any ORM unless specifically instructed for a given project

### Python
- use uv for project and version management
- use Python 3.12+
- use FastAPI for REST API
- use SQLAlchemy for SQL access
- use Pydantic
- use pytest for unit tests
- use LangChain / LangGraph for AI agents development

## CI/CD
- use GitHub Actions for CI/CD
- prepare deployments for Kubernetes using helm charts
- prepare deployments for using with ArgoCD app-od-app approach
- use 'kustomize' with 'base' and 'overlays' directories for deployments
- run linting before committing (use pre-commit hook)
- run all tests before merging to main; block merging if any test fails
- build only changed components in CI

## General Instructions

### Planning 
- unless explicitly asked to implement immediately, always prepare a detailed working plan in advance
- Always read through the instructions, either in the prompt or in a specific requirements file.
- If the requirements are written in Stages, always work only on one Stage at a time.
- Always prepare a detailed task list for the given Stage in a file `tasks-stage-xx.md` in the project root directory so that I can review the plan. Use checkboxes so that we can follow up the progress later on.
- While planning, do NOT implement anything yet, focus on analysis and planning. You can use a specific section Open Questiions to ask questions I need to answer before the implementation starts. Please, ask also to confirm any assumptions and defaults you have made in the planning. You will help me a lot with explicitly asking questions, as my requirements might be unclear or incomplete.
- While planning, do NOT touch any code or files. Do NOT refactor any files or pieces of code that seem unused at this time, we will need them later on. Prepare the task list and get back to me for review before changing anything.

### Implementation
- do ONLY what asked for
- don't refactor code outside what you have been asked for
- don't delete any "unused" code without approval
- NEVER merge anything
- if a detailed task list is available, mark the checkboxes as you go

### Safety and permissions
Allowed: read/list files, lint/test single files, git push to a new branch, PR creation
Ask first: installs, deletes, full builds

### PR checklist
- format and type check pass
- unit tests green
- diff small with a short summary

### When stuck or working for a long time
- take a break and let me review the progress
- ask a question
- break, summarize the progress and propose a plan going forward
- store the progress in the project root directory as `progress-stage-xx.md` and the current plan in `fwd-stage-xx.md`




## Examples

### Serialization Example
This is the example of a multi-type field in a data class.
Please, note that the required behavior is to accept the primitives or arrays WITHOUT specifying the "value" or "values" field.
Preferred implementation would be to use the inner classes in the interface, like this:
```kotlin
@Serializable
sealed interface MetadataValue {
    @Serializable
    data class MetadataSingle(val value: String) : MetadataValue
    @Serializable
    data class MetadataList(val values: List<String>) : MetadataValue
}
```

This is the example interface, classes and custom serializer:
```kotlin
@Serializable
sealed interface MetadataValue

@Serializable
data class MetadataSingle(val value: String) : MetadataValue

@Serializable
data class MetadataList(val values: List<String>) : MetadataValue

/**
 * Parse a JSON string representing metadata into a Map<String, MetadataValue>.
 * Values can be a string or an array of strings. Other types will be stringified.
 */
fun parseMetadataJson(jsonText: String): Map<String, MetadataValue> {
    val root = Json.parseToJsonElement(jsonText)
    if (root !is kotlinx.serialization.json.JsonObject) return emptyMap()
    val out = mutableMapOf<String, MetadataValue>()
    for ((k, v) in root) {
        out[k] = when (v) {
            is JsonPrimitive -> MetadataSingle(v.content)
            is JsonArray -> MetadataList(v.mapNotNull { (it as? JsonPrimitive)?.content })
            else -> MetadataSingle(v.toString())
        }
    }
    return out
}

/**
 * Custom serializer for a single MetadataValue that supports the nested forms:
 * {"value":"A"} and {"values":["B","C"]}.
 * Also leniently accepts primitives and arrays.
 */
object MetadataValueSerializer : KSerializer<MetadataValue> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("MetadataValue")

    override fun deserialize(decoder: Decoder): MetadataValue {
        val jd = decoder as? JsonDecoder ?: error("MetadataValueSerializer requires Json")
        val elem = jd.decodeJsonElement()
        return when (elem) {
            is JsonObject -> {
                val v = elem["value"]
                val vs = elem["values"]
                when {
                    v is JsonPrimitive -> MetadataSingle(v.content)
                    vs is JsonArray -> MetadataList(vs.mapNotNull { (it as? JsonPrimitive)?.content })
                    // Fallbacks
                    elem.size == 1 && elem.values.firstOrNull() is JsonPrimitive ->
                        MetadataSingle((elem.values.first() as JsonPrimitive).content)
                    elem.size == 1 && elem.values.firstOrNull() is JsonArray ->
                        MetadataList(((elem.values.first() as JsonArray).mapNotNull { (it as? JsonPrimitive)?.content }))
                    else -> MetadataSingle(elem.toString())
                }
            }
            is JsonPrimitive -> MetadataSingle(elem.content)
            is JsonArray -> MetadataList(elem.mapNotNull { (it as? JsonPrimitive)?.content })
            else -> MetadataSingle(elem.toString())
        }
    }

    override fun serialize(encoder: Encoder, value: MetadataValue) {
        val je = encoder as? JsonEncoder ?: error("MetadataValueSerializer requires Json")
        val obj = when (value) {
            is MetadataSingle -> buildJsonObject { put("value", JsonPrimitive(value.value)) }
            is MetadataList -> buildJsonObject {
                put("values", JsonArray(value.values.map { JsonPrimitive(it) }))
            }
        }
        je.encodeJsonElement(obj)
    }
}
```