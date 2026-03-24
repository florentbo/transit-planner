---
id: T01
parent: S01
milestone: M002
provides:
  - Multi-stage Dockerfile at repo root
  - PORT env var support in application.yml
  - .dockerignore for clean build context
  - DEPLOYMENT.md with full Cloud Run docs
key_files:
  - Dockerfile
  - .dockerignore
  - DEPLOYMENT.md
  - backend/src/main/resources/application.yml
key_decisions:
  - Dockerfile at repo root for OpenAPI codegen access
  - Used content from feature branch but updated DEPLOYMENT.md for STIB_API_KEY
  - Applied PORT change only to main's application.yml (branch version lacked STIB config)
patterns_established:
  - Repo-root Dockerfile for multi-directory build context
observability_surfaces:
  - Cloud Run service URL
duration: 5min
verification_result: passed
completed_at: 2026-03-03
blocker_discovered: false
---

# T01: Add deployment infrastructure + verify Cloud Run

**Multi-stage Docker build with Cloud Run PORT env var, .dockerignore, and DEPLOYMENT.md — deployed and verified live**

## What Happened

Copied Dockerfile and .dockerignore from origin/feature/google-cloud-run-deployment. Updated DEPLOYMENT.md to add STIB_API_KEY environment variable documentation. Changed application.yml server.port from 8080 to ${PORT:8080} while preserving all STIB API config (the branch version lacked STIB config since it predated v1.0).

User deployed to Cloud Run and verified the service is live at https://transit-planner-backend-621870148637.europe-west1.run.app returning real STIB departure data.

## Verification

| # | Command | Exit Code | Verdict | Duration |
|---|---------|-----------|---------|----------|
| 1 | `./gradlew compileJava` | 0 | pass | ~10s |
| 2 | `curl .../api/departures` (user) | 200 | pass | — |

## Deviations

None.

## Known Issues

None.

## Files Created/Modified

- `Dockerfile` — Multi-stage build: JDK build stage (bootJar), JRE run stage
- `.dockerignore` — Excludes frontend/, .git/, backend/build/, backend/.gradle/, *.md
- `DEPLOYMENT.md` — Full Cloud Run docs with STIB_API_KEY, troubleshooting
- `backend/src/main/resources/application.yml` — PORT env var with 8080 fallback
