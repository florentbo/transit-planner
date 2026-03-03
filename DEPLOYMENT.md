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

## Environment Variables

The following environment variables must be set on the Cloud Run service:

| Variable | Description | Required |
|----------|-------------|----------|
| `STIB_API_KEY` | API key for the STIB/MIVB Open Data platform | Yes |
| `CORS_ALLOWED_ORIGINS` | Comma-separated allowed origins for CORS | Yes |
| `PORT` | HTTP port Cloud Run will send traffic to (injected automatically) | Auto |

## CI/CD (GitHub Actions)

Pushes to `main` that touch `backend/`, `api-spec/`, `Dockerfile`, or `.dockerignore` automatically deploy via GitHub Actions.

**Workflow:** `.github/workflows/deploy-backend.yml`

**Pipeline:** Checkout -> Auth (Service Account Key) -> Docker build -> Push to Artifact Registry -> `gcloud run deploy`

### GitHub Secrets Required

| Secret | Description |
|--------|-------------|
| `GCP_SA_KEY` | Service account key JSON for `github-actions@transport-login.iam.gserviceaccount.com` |
| `STIB_API_KEY` | STIB/MIVB Open Data API key |
| `CORS_ALLOWED_ORIGINS` | e.g. `http://localhost:5173,https://transit-planner-app.netlify.app` |

### GCP Service Account Setup

If you need to recreate the service account from scratch:

```bash
# 1. Create service account
gcloud iam service-accounts create github-actions \
  --display-name="GitHub Actions" --project=transport-login

# 2. Grant required roles
SA=github-actions@transport-login.iam.gserviceaccount.com

gcloud projects add-iam-policy-binding transport-login \
  --member="serviceAccount:$SA" --role="roles/run.admin" --quiet
gcloud projects add-iam-policy-binding transport-login \
  --member="serviceAccount:$SA" --role="roles/storage.admin" --quiet
gcloud projects add-iam-policy-binding transport-login \
  --member="serviceAccount:$SA" --role="roles/iam.serviceAccountUser" --quiet
gcloud projects add-iam-policy-binding transport-login \
  --member="serviceAccount:$SA" --role="roles/artifactregistry.writer" --quiet

# 3. Generate key and set as GitHub secret
gcloud iam service-accounts keys create /tmp/key.json --iam-account=$SA
gh secret set GCP_SA_KEY --repo florentbo/transit-planner < /tmp/key.json
rm /tmp/key.json
```

## Manual Deploying

### Local Docker build (fast, ~2-3 min)

```bash
./deploy-backend-local.sh
```

Builds locally, pushes to Artifact Registry, deploys to Cloud Run. Requires Docker and `gcloud auth configure-docker europe-west1-docker.pkg.dev`.

### Remote Cloud Build (~10 min)

```bash
./deploy-backend.sh
```

Uploads source to Cloud Build for remote Docker build. No local Docker needed.

Both scripts read `STIB_API_KEY` and `CORS_ALLOWED_ORIGINS` from env vars. Copy to `.local.sh` variants (gitignored) to hardcode credentials.

## Verifying

```bash
# Check service status
gcloud run services describe transit-planner-backend --region europe-west1

# Test the API
curl https://transit-planner-backend-621870148637.europe-west1.run.app/api/departures

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
