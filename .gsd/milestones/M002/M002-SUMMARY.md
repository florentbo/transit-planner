---
id: M002
provides:
  - Multi-stage Dockerfile for Spring Boot backend
  - Cloud Run deployment with STIB_API_KEY env var
  - DEPLOYMENT.md documenting full process
key_decisions:
  - Dockerfile at repo root (not backend/) for OpenAPI codegen access
  - Multi-stage build — eclipse-temurin:25-jdk for build, eclipse-temurin:25-jre for run
  - PORT env var with ${PORT:8080} fallback
patterns_established:
  - Repo-root Dockerfile when build context needs multiple sibling directories
  - Spring Boot PORT — ${PORT:8080} for Cloud Run + local dev compatibility
observability_surfaces:
  - Cloud Run service URL as health check endpoint
requirement_outcomes:
  - id: R007
    from_status: active
    to_status: validated
    proof: Service live at https://transit-planner-backend-621870148637.europe-west1.run.app returning real STIB data
duration: 5min + async user deployment
verification_result: passed
completed_at: 2026-03-03
---

# M002: Deploy to GCloud

**Multi-stage Docker build deployed to Google Cloud Run — backend serving real STIB departures at a public URL**

## What Happened

Single slice brought deployment infrastructure from the feature/google-cloud-run-deployment branch onto main. Created multi-stage Dockerfile at repo root (JDK build stage → JRE run stage), .dockerignore excluding frontend/.git/build artifacts, DEPLOYMENT.md with full Cloud Run deployment docs including STIB_API_KEY. Updated application.yml to read PORT from environment with 8080 fallback. User deployed to Cloud Run and verified the service is live.

## Cross-Slice Verification

- `./gradlew compileJava` passes with PORT env var change
- Dockerfile structure verified by inspection (multi-stage, correct COPY paths)
- Live service verified at https://transit-planner-backend-621870148637.europe-west1.run.app/api/departures

## Requirement Changes

- R007: active → validated — Service deployed and returning real STIB data

## Forward Intelligence

### What the next milestone should know
- Backend is accessible at https://transit-planner-backend-621870148637.europe-west1.run.app
- Frontend still points to localhost:8080 — needs updating to use Cloud Run URL
- STIB_API_KEY must be passed as --set-env-vars during gcloud run deploy

### What's fragile
- No CI/CD — deployment is manual via gcloud CLI

### Authoritative diagnostics
- `curl https://transit-planner-backend-621870148637.europe-west1.run.app/api/departures | jq .` — verify service health

### What assumptions changed
- None — deployment was straightforward
