# justfile

set shell := ["bash", "-c"]

# =============================================================================
# 🟢 GLOBAL HELPERS
# =============================================================================

default:
    @just --list

# Initialize the repo (Download dependencies, prepare protos)
init: proto-all
    @echo "Initializing Kotlin..."
    ./gradlew --quiet dependencies
    @echo "Initializing Python..."
    just py-sync-all
    @echo "Initializing Vue..."
    just vue-install-all

# Clean everything
clean:
    ./gradlew clean
    find . -type d -name "__pycache__" -exec rm -rf {} +
    find . -type d -name "node_modules" -exec rm -rf {} +
    find . -type d -name ".venv" -exec rm -rf {} +
    rm -rf apps/*/dist

# =============================================================================
# 🟡 SHARED PROTOCOLS (The Glue)
# =============================================================================

# Generates Protos for ALL languages (Kotlin, Python, JS)
proto-all:
    ./gradlew :libs:shared-proto:assemble

# Specific helpers if you only need one language refreshed
proto-py:
    ./gradlew :libs:shared-proto:preparePythonPackage

proto-js:
    ./gradlew :libs:shared-proto:prepareJsPackage

# =============================================================================
# 🔵 KOTLIN SERVICES (Spring Boot & Ktor)
# =============================================================================

# Build a Kotlin service (JAR)
# Usage: just build-kt payment-service-spring
build-kt service:
    ./gradlew :apps:{{service}}:build

# Run a Kotlin service locally
run-kt service:
    ./gradlew :apps:{{service}}:run

# Deploy Kotlin service to Local K3s (using Jib)
deploy-kt service:
    ./gradlew :apps:{{service}}:jibBuildTar
    nerdctl -n k8s.io load -i apps/{{service}}/build/jib-image.tar
    @echo "🚀 {{service}} loaded into K3s!"

# =============================================================================
# 🐍 PYTHON SERVICES (uv)
# =============================================================================

# Sync dependencies for ALL python apps
py-sync-all: proto-py
    @for dir in apps/*-py; do \
        echo "Syncing $dir..."; \
        (cd $dir && uv sync); \
    done

# Run tests for a specific Python service
# Usage: just test-py recommendation-service-py
test-py service: proto-py
    cd apps/{{service}} && uv run pytest

# Run Python service locally
run-py service: proto-py
    cd apps/{{service}} && uv run fastapi dev src/main.py

# Deploy Python service to Local K3s (Standard Docker build)
deploy-py service: proto-py
    cd apps/{{service}} && uv sync # Ensure lockfile is fresh
    nerdctl -n k8s.io build -t {{service}}:local apps/{{service}}
    @echo "🚀 {{service}} loaded into K3s!"

# =============================================================================
# 🎨 VUE FRONTENDS (npm + vite)
# =============================================================================

# Install dependencies for ALL vue apps
vue-install-all: proto-js
    @for dir in apps/web-*; do \
        echo "Installing $dir..."; \
        (cd $dir && npm install); \
    done

# Build Vue app for production
# Usage: just build-vue web-backoffice
build-vue service: proto-js
    cd apps/{{service}} && npm run build

# Run Vue app dev server
run-vue service: proto-js
    cd apps/{{service}} && npm run dev

# Deploy Vue service to Local K3s (Docker build of /dist)
deploy-vue service:
    just build-vue {{service}}
    nerdctl -n k8s.io build -t {{service}}:local apps/{{service}}
    @echo "🚀 {{service}} loaded into K3s!"

# =============================================================================
# ⚓ KUBERNETES & DEBUGGING
# =============================================================================

# Quick restart of a pod in K3s to pick up the new 'local' image
restart service:
    kubectl rollout restart deployment/{{service}}

# Port forward specific services for local debugging (IntelliJ)
debug-tunnel:
    @echo "Forwarding services to localhost..."
    @trap 'kill 0' EXIT; \
    kubectl port-forward svc/postgres 5432:5432 & \
    kubectl port-forward svc/wiremock 9090:8080 & \
    wait