# S01: Cloud Run Deployment Infrastructure

**Goal:** Deploy backend to Google Cloud Run with proper Docker build and documentation.
**Demo:** Backend is live at a public URL, returning real STIB departure data.

## Must-Haves

- Dockerfile at repo root builds a runnable Spring Boot JAR
- application.yml reads PORT env var with fallback to 8080
- STIB API config preserved in application.yml
- .dockerignore excludes frontend, .git, build artifacts
- DEPLOYMENT.md documents full deployment process including STIB_API_KEY

## Tasks

- [x] **T01: Add deployment infrastructure files + verify Cloud Run** `est:5m`
  - Why: Bring deployment files from feature branch onto main and deploy to Cloud Run
  - Files: `Dockerfile`, `.dockerignore`, `DEPLOYMENT.md`, `backend/src/main/resources/application.yml`
  - Do: Copy Dockerfile and .dockerignore from feature branch. Update DEPLOYMENT.md with STIB_API_KEY docs. Change application.yml port to ${PORT:8080} while preserving STIB config.
  - Verify: `./gradlew compileJava` + user deploys and verifies live service
  - Done when: Cloud Run service is live and responding with real STIB departure data

## Files Likely Touched

- `Dockerfile`
- `.dockerignore`
- `DEPLOYMENT.md`
- `backend/src/main/resources/application.yml`
