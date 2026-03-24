---
id: S01
parent: M002
milestone: M002
provides:
  - Multi-stage Dockerfile for Spring Boot backend at repo root
  - PORT env var support in application.yml (Cloud Run compatible)
  - .dockerignore excluding frontend, .git, build artifacts
  - DEPLOYMENT.md documenting full Cloud Run deployment with STIB_API_KEY
requires: []
affects: []
key_files:
  - Dockerfile
  - .dockerignore
  - DEPLOYMENT.md
  - backend/src/main/resources/application.yml
key_decisions:
  - Dockerfile at repo root (not backend/) because Gradle build needs ../api-spec/openapi.yaml
  - Multi-stage build — eclipse-temurin:25-jdk for build, eclipse-temurin:25-jre for run
  - PORT env var with ${PORT:8080} fallback
  - STIB_API_KEY documented as required --set-env-vars in deploy command
patterns_established:
  - Repo-root Dockerfile when build context needs multiple sibling directories
  - Spring Boot PORT — ${PORT:8080} for Cloud Run + local dev compatibility
observability_surfaces:
  - Cloud Run service URL as health check
drill_down_paths:
  - tasks/T01-SUMMARY.md
duration: 5min + async deployment
verification_result: passed
completed_at: 2026-03-03
---

# S01: Cloud Run Deployment Infrastructure

**Multi-stage Docker build (eclipse-temurin JDK/JRE) with Cloud Run PORT env var, .dockerignore, and DEPLOYMENT.md documenting STIB_API_KEY deployment**

## What Happened

Brought deployment files from origin/feature/google-cloud-run-deployment onto main. Created multi-stage Dockerfile at repo root using eclipse-temurin:25-jdk (build) and eclipse-temurin:25-jre (run). Updated application.yml server.port from 8080 to ${PORT:8080} while preserving all STIB API config. Created .dockerignore excluding frontend/, .git/, backend/build/, backend/.gradle/, and *.md. Created DEPLOYMENT.md documenting full Cloud Run deployment including STIB_API_KEY as required env var.

Cloud Run service deployed and verified live at https://transit-planner-backend-621870148637.europe-west1.run.app.

## Verification

- `./gradlew compileJava` — passes with PORT env var change
- Dockerfile, .dockerignore, DEPLOYMENT.md all exist at correct paths
- Live service verified by user returning real STIB departure data
- Phase verification: 5/5 observable truths verified

## Requirements Validated

- R007 — Backend deployed to Cloud Run, accessible at public URL with STIB_API_KEY

## Deviations

None — plan executed exactly as written.

## Known Limitations

- No CI/CD pipeline — deployment is manual via gcloud CLI
- Frontend still points to localhost:8080

## Files Created/Modified

- `Dockerfile` — Multi-stage build: JDK build stage runs bootJar, JRE run stage executes JAR
- `.dockerignore` — Excludes frontend/, .git/, backend/build/, backend/.gradle/, *.md
- `DEPLOYMENT.md` — Full Cloud Run deployment docs with prerequisites, env vars, troubleshooting
- `backend/src/main/resources/application.yml` — Changed port to ${PORT:8080}, STIB config preserved

## Forward Intelligence

### What the next slice should know
- Backend is live on Cloud Run at the URL above
- Frontend can be updated to point at Cloud Run URL instead of localhost:8080

### What's fragile
- Manual deployment — no CI/CD means easy to deploy stale code

### Authoritative diagnostics
- `curl https://transit-planner-backend-621870148637.europe-west1.run.app/api/departures | jq .`

### What assumptions changed
- None
