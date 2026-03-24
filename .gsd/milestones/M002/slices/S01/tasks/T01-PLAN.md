# T01: Add deployment infrastructure + verify Cloud Run

**Slice:** S01 — **Milestone:** M002

## Description

Bring deployment files from feature branch onto main and deploy to Cloud Run.

## Must-Haves

- [x] Dockerfile exists at repo root and builds a runnable Spring Boot JAR
- [x] application.yml reads PORT env var with fallback to 8080
- [x] STIB API config preserved in application.yml
- [x] .dockerignore excludes frontend, .git, build artifacts
- [x] DEPLOYMENT.md documents full deployment process including STIB_API_KEY
- [x] Cloud Run service live and returning real STIB data

## Files

- `Dockerfile`
- `.dockerignore`
- `DEPLOYMENT.md`
- `backend/src/main/resources/application.yml`
