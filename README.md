# df-mono
a polyglot monorepo (Kotlin, Python, Vue)

### Summary of the Monorepo Architecture

We have designed a Polyglot Monorepo optimized for **K3s (Dev)** and **Azure/ArgoCD (Prod)**.

- **Structure:**

    - **`apps/`**: Contains Microservices (Kotlin/Spring, Kotlin/Ktor, Python/FastAPI) and Frontends (Vue/Vite).
    - **`libs/`**: Shared code. Crucially, `libs/shared-proto` is the Single Source of Truth for data contracts.
    - **`build-logic/`**: Custom Gradle Convention Plugins (`my.kotlin-spring`, `my.python-common`) to keep service build files minimal.

- **Build System:**
    - **Gradle:** The master orchestrator. Handles Kotlin builds directly and triggers Python/JS proto generation.
    - **Just:** The command runner. It abstracts the complexity, allowing uniform commands (`just build-kt`, `just deploy-py`) regardless of the underlying language.
    - **Jib:** Builds Kotlin containers without Dockerfiles.
    - **uv:** Manages Python dependencies and environments incredibly fast.
    - **npm:** Manages Vue frontend builds.

- **Infrastructure:**
    - **Local:** Rancher Desktop (K3s) with `nerdctl` for direct image loading (no registry push needed).
    - **CI/CD:** GitHub Actions. Tag-based releases (`service-name/v1.0.0`). Automated "Project Resolver" to determine build type (Jib vs Docker) dynamically.

This repository hosts the microservices ecosystem, featuring a hybrid build system orchestrated by `just`.

## 🏗 Project Structure

| Directory | Description |
| :--- | :--- |
| **`apps/`** | Microservices and Frontends. |
| &nbsp;&nbsp; `*-spring` | Kotlin services using Spring Boot 3. |
| &nbsp;&nbsp; `*-ktor` | Kotlin services using Ktor 3. |
| &nbsp;&nbsp; `*-py` | Python services using FastAPI & `uv`. |
| &nbsp;&nbsp; `web-*` | Vue 3 frontends using Vite. |
| **`libs/`** | Shared libraries and contracts. |
| &nbsp;&nbsp; `shared-proto` | **The Source of Truth.** Contains `.proto` files that compile to Kotlin JARs, Python packages, and NPM packages. |
| **`build-logic/`** | Custom Gradle Convention Plugins (The "Brains" of the Kotlin build). |
| **`gradle/`** | Version Catalog (`libs.versions.toml`) defining dependencies for the whole repo. |
| **`justfile`** | The command runner. **Start here.** |

---

## 🚀 Getting Started

### Prerequisites
* **JDK 21+** (Eclipse Temurin recommended)
* **Just** (Command runner)
* **uv** (Python package manager replacement)
* **Node.js 20+**
* **Rancher Desktop** (with `dockerd` or `containerd` enabled).
    * *Note: If using containerd, ensure `nerdctl` is in your PATH.*

### Initialization
Run this once after cloning to generate local Protocol Buffer packages for all languages.

```bash
just init
```

---

## Building & Running Locally

We use `just` to abstract the underlying tools (Gradle, uv, npm).

### 1. Protocol Buffers (The Glue)
Before building any app, ensure the shared data contracts are up to date.

```bash
just proto-all   # Generates Kotlin, Python, and JS artifacts
```

### 2. Microservices

You can run services natively (on host) or deploy them to your local K3s cluster.

|**Action**|**Kotlin (Spring/Ktor)**|**Python (FastAPI)**|**Vue (Frontend)**|
|---|---|---|---|
|**Build**|`just build-kt <service>`|`just py-sync-all`|`just build-vue <service>`|
|**Run (Host)**|`just run-kt <service>`|`just run-py <service>`|`just run-vue <service>`|
|**Test**|`./gradlew test`|`just test-py <service>`|`npm run test`|


### 3. Deploying to Rancher Desktop (K3s)

We do **not** push to a registry for local dev. We build directly into the K3s container cache.

```bash
# Example: Deploy Payment Service
just deploy-kt payment-service-spring
```

- **Kotlin:** Uses Jib to build a tarball -> `nerdctl load -n k8s.io`

- **Python/Vue:** Uses `nerdctl build` directly.


**Usage in K8s Manifests:** Always use `imagePullPolicy: Never`.


```yaml
containers:
  - name: app
    image: payment-service-spring:local  # Tag is always :local
    imagePullPolicy: Never
```

---

## 🐞 Local Debugging (Hybrid Mode)

To debug a service in your IDE while it talks to other services in the cluster:

1. **Deploy everything** to K3s first: `helm install ...`

2. **Scale down** the service you want to debug to 0 replicas.

3. **Start the Tunnel**:

    ```bash
    just debug-tunnel
    ```

   _This forwards Postgres, Wiremock, and other internal services to `localhost`._

4. **Debug in IDE**:

    - **IntelliJ:** Select the run configuration (e.g., "Payment Service (Local)").

    - **Profile:** Ensure `application-local.yaml` is active (points to `localhost:5432` instead of `postgres:5432`).

---

## ➕ Creating New Modules

### A. New Kotlin Service (Spring Boot)

1. Create folder `apps/my-new-service-spring`.

2. Create `build.gradle.kts`:

    ```kotlin
    plugins {
        id("ai-platform.kotlin-spring")
    }
    dependencies {
        implementation(projects.libs.sharedProto) // If needing protos
    }
    ```

3. Create standard `src/main/kotlin` structure.


### B. New Kotlin Service (Ktor)

1. Create folder `apps/my-new-service-ktor`.

2. Create `build.gradle.kts`:

    ```kotlin
    plugins {
        id("ai-platform.kotlin-ktor")
    }
    application {
        mainClass.set("com.example.newservice.ApplicationKt")
    }
    dependencies {
        implementation(projects.libs.sharedProto)
    }
    ```


### C. New Python Service

1. Create folder `apps/my-new-service-py`.

2. Initialize `uv`:

    ```bash
    cd apps/my-new-service-py
    uv init
    ```

3. Edit `pyproject.toml` to depend on the shared protos:

    ```toml
    [tool.uv.sources]
    shared-proto = { path = "../../libs/shared-proto/build/python-package", editable = true }
    ```

4. Create `Dockerfile` (Copy from existing Python service).

---

## 📦 Release Process

Releases are triggered by **Git Tags**.

1. **Tag:** `git tag payment-service-spring/v1.0.0`

2. **Push:** `git push origin --tags`

3. **CI:** GitHub Actions will:

    - Detect the service name.

    - Resolve the build type (Jib vs Docker).

    - Build and push the image to ACR (`myregistry.azurecr.io/payment-service-spring:1.0.0`).
