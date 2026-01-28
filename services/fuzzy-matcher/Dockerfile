# Stage 1: Build env
FROM python:3.12-slim-bookworm AS builder
COPY --from=ghcr.io/astral-sh/uv:latest /uv /bin/uv

WORKDIR /app
# Install dependencies separate from code to utilize Docker layer caching
COPY pyproject.toml uv.lock ./
# --frozen ensures we don't update versions, just sync what's in lock
# --no-dev excludes test dependencies
RUN uv sync --frozen --no-dev --no-install-project

# Stage 2: Runtime
FROM python:3.12-slim-bookworm
WORKDIR /app

# Copy virtualenv from builder
COPY --from=builder /app/.venv /app/.venv
# Copy actual code
COPY src ./src

# Add venv to path
ENV PATH="/.venv/bin:$PATH"
USER 1000:1000
CMD ["fastapi", "run", "src/main.py", "--port", "8080"]