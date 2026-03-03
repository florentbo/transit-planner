---
phase: 01-deploy-to-gcloud
plan: 01
subsystem: infra
tags: [docker, cloud-run, gcp, spring-boot, deployment]

# Dependency graph
requires: []
provides:
  - "Multi-stage Dockerfile for Spring Boot backend at repo root"
  - "PORT env var support in application.yml (Cloud Run compatible)"
  - ".dockerignore excluding frontend, .git, build artifacts"
  - "DEPLOYMENT.md documenting full Cloud Run deployment process with STIB_API_KEY"
affects: [cloud-run-deployment, ci-cd]

# Tech tracking
tech-stack:
  added: [eclipse-temurin:25-jdk, eclipse-temurin:25-jre, Docker multi-stage build]
  patterns: [repo-root Dockerfile for multi-directory build context, PORT env var with local fallback]

key-files:
  created:
    - Dockerfile
    - .dockerignore
    - DEPLOYMENT.md
  modified:
    - backend/src/main/resources/application.yml

key-decisions:
  - "Dockerfile at repo root (not backend/) because Gradle build needs ../api-spec/openapi.yaml for OpenAPI codegen"
  - "Multi-stage build: eclipse-temurin:25-jdk for build, eclipse-temurin:25-jre for run (smaller image)"
  - "PORT env var with ${PORT:8080} fallback — Cloud Run injects PORT, local dev uses 8080"
  - "STIB_API_KEY documented as required --set-env-vars in deploy command"

patterns-established:
  - "Repo-root Dockerfile: when build context needs multiple sibling directories"
  - "Spring Boot PORT: use ${PORT:8080} so local dev and Cloud Run work without config changes"

requirements-completed: [DEPLOY-01]

# Metrics
duration: 5min
completed: 2026-03-03
---

# Phase 01 Plan 01: Deploy to GCloud Summary

**Multi-stage Docker build (eclipse-temurin JDK/JRE) with Cloud Run PORT env var, .dockerignore, and DEPLOYMENT.md documenting STIB_API_KEY deployment**

## Performance

- **Duration:** ~5 min (automation) + async user deployment
- **Started:** 2026-03-03T17:21:02Z
- **Completed:** 2026-03-03T17:44:40Z
- **Tasks:** 2 of 2 (both complete)
- **Files modified:** 4

## Accomplishments

- Created multi-stage Dockerfile at repo root using eclipse-temurin:25-jdk (build) and eclipse-temurin:25-jre (run)
- Updated application.yml server.port from `8080` to `${PORT:8080}` while preserving all STIB API config
- Created .dockerignore excluding frontend/, .git/, backend/build/, backend/.gradle/, and *.md
- Created DEPLOYMENT.md documenting full Cloud Run deployment including STIB_API_KEY as required env var
- Cloud Run service deployed and verified live at https://transit-planner-backend-621870148637.europe-west1.run.app — returning real STIB departure data

## Task Commits

Each task was committed atomically:

1. **Task 1: Add deployment infrastructure files from feature branch** - `b55bdf3` (chore)
2. **Task 2: Verify deployment to Google Cloud Run** - checkpoint approved (user deployed, service live)

**Plan metadata:** `00bafba` (docs: complete deployment infrastructure plan)

## Files Created/Modified

- `Dockerfile` - Multi-stage build: JDK build stage runs bootJar, JRE run stage executes JAR
- `.dockerignore` - Excludes frontend/, .git/, backend/build/, backend/.gradle/, *.md
- `DEPLOYMENT.md` - Full Cloud Run deployment docs with prerequisites, env vars (STIB_API_KEY), deploy command, verification, troubleshooting
- `backend/src/main/resources/application.yml` - Changed port from `8080` to `${PORT:8080}`, all STIB config preserved

## Decisions Made

- Used content from `origin/feature/google-cloud-run-deployment` for Dockerfile and .dockerignore, but updated DEPLOYMENT.md to add STIB_API_KEY environment variable documentation
- Applied PORT change only to main branch's application.yml (branch version lacked STIB config)

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

None.

## User Setup Required

**Cloud Run deployment requires manual steps.** See [DEPLOYMENT.md](../../DEPLOYMENT.md) for:
- `STIB_API_KEY` must be passed as `--set-env-vars "STIB_API_KEY=your-key-here"` in the deploy command
- GCP project with billing enabled required
- gcloud CLI authenticated

## Next Phase Readiness

- Backend is live on Cloud Run at https://transit-planner-backend-621870148637.europe-west1.run.app
- Service returns real-time STIB departure data (verified)
- Frontend can be updated to point at Cloud Run URL instead of localhost:8080
- No blockers — deployment infrastructure complete

---
*Phase: 01-deploy-to-gcloud*
*Completed: 2026-03-03*
