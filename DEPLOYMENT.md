# Deploying the Backend to Google Cloud Run

## Overview

The backend (Java 25, Spring Boot 4.0, Gradle 9.2.1) is deployed to Google Cloud Run's free tier as a Docker container. Cloud Run scales to zero when idle and provides 2M free requests/month.

**Service URL**: `https://transit-planner-backend-621870148637.europe-west1.run.app`

## Prerequisites

- [Google Cloud SDK](https://cloud.google.com/sdk/docs/install) (`gcloud` CLI)
- A GCP project with billing enabled
- Docker (only needed for local testing, Cloud Build handles remote builds)

## Architecture

```
Repo root
├── Dockerfile          ← Multi-stage build (build context = repo root)
├── .dockerignore       ← Excludes frontend, .git, build artifacts
├── api-spec/           ← OpenAPI spec (needed at build time)
└── backend/            ← Spring Boot app
```

The Dockerfile is at the repo root (not in `backend/`) because the Gradle build references `../api-spec/openapi.yaml` for OpenAPI code generation. The Docker build context needs access to both directories.

## What Was Configured

### 1. Dockerfile (multi-stage build)

**Stage 1 — Build**: Uses `eclipse-temurin:25-jdk` to run `./gradlew bootJar`, which compiles the app, generates OpenAPI stubs, and produces a fat JAR.

**Stage 2 — Run**: Uses `eclipse-temurin:25-jre` (smaller image) to run the JAR. Only the built artifact is copied from the build stage.

### 2. .dockerignore

Excludes `frontend/`, `.git/`, `backend/build/`, `backend/.gradle/`, and markdown files to keep the build context small and fast.

### 3. application.yml — PORT env var

```yaml
server:
  port: ${PORT:8080}
```

Cloud Run injects a `PORT` environment variable. Spring Boot reads it at startup. Falls back to `8080` for local development.

### 4. GCP APIs enabled

- `run.googleapis.com` — Cloud Run
- `artifactregistry.googleapis.com` — stores Docker images
- `cloudbuild.googleapis.com` — builds Docker images remotely

### 5. Cloud Run service configuration

| Setting | Value |
|---------|-------|
| Region | `europe-west1` (Belgium) |
| Memory | 512Mi |
| CPU | 1 |
| Min instances | 0 (scales to zero) |
| Max instances | 1 |
| Port | 8080 |
| Auth | Unauthenticated (public) |

## Deploying

From the repo root:

```bash
gcloud run deploy transit-planner-backend \
  --source . \
  --region europe-west1 \
  --allow-unauthenticated \
  --memory 512Mi \
  --cpu 1 \
  --min-instances 0 \
  --max-instances 1 \
  --port 8080
```

The `--source .` flag tells Cloud Build to upload the source, build the Docker image remotely using the Dockerfile, push it to Artifact Registry, and deploy it to Cloud Run. No local Docker build needed.

## Verifying

```bash
# Check service status
gcloud run services describe transit-planner-backend --region europe-west1

# Test the API
curl https://transit-planner-backend-621870148637.europe-west1.run.app/api/routes

# View logs
gcloud run services logs read transit-planner-backend --region europe-west1
```

## Troubleshooting

### gcloud SSL errors

If you see `CERTIFICATE_VERIFY_FAILED` errors, point gcloud to the system CA bundle:

```bash
gcloud config set core/custom_ca_certs_file /etc/pki/tls/certs/ca-bundle.crt
```

### Cold starts

With `min-instances=0`, the first request after idle time takes ~10-15s (JVM startup). Set `--min-instances 1` to avoid cold starts (costs more).

### Memory

512Mi is the minimum for Spring Boot + JVM. If you see OOM crashes in logs, increase to `--memory 1Gi`.
