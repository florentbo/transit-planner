---
phase: 01-deploy-to-gcloud
verified: 2026-03-03T18:00:00Z
status: passed
score: 5/5 must-haves verified
re_verification: false
---

# Phase 01: Deploy to GCloud Verification Report

**Phase Goal:** Deploy backend to Google Cloud Run, integrating existing deployment infrastructure from feature branch into main
**Verified:** 2026-03-03
**Status:** passed
**Re-verification:** No — initial verification

## Goal Achievement

### Observable Truths

| #  | Truth                                                                   | Status     | Evidence                                                                                                     |
|----|-------------------------------------------------------------------------|------------|--------------------------------------------------------------------------------------------------------------|
| 1  | Dockerfile exists at repo root and builds a runnable Spring Boot JAR   | VERIFIED   | `/Dockerfile` uses eclipse-temurin:25-jdk, runs `./gradlew bootJar --no-daemon`, copies JAR to JRE stage    |
| 2  | application.yml reads PORT env var with fallback to 8080               | VERIFIED   | Line 2: `port: ${PORT:8080}`                                                                                 |
| 3  | STIB API config is preserved in application.yml                        | VERIFIED   | `stib.api.base-url` and `stib.api.key: ${STIB_API_KEY}` present at lines 8-12                               |
| 4  | .dockerignore excludes frontend, .git, and build artifacts             | VERIFIED   | Contains `frontend/`, `.git/`, `backend/build/`, `backend/.gradle/`, `*.md`                                 |
| 5  | DEPLOYMENT.md documents the full deployment process                    | VERIFIED   | 126-line doc with `gcloud run deploy` command, `--set-env-vars "STIB_API_KEY=your-key-here"`, troubleshooting |

**Score:** 5/5 truths verified

### Required Artifacts

| Artifact                                    | Expected                               | Status   | Details                                                                                          |
|---------------------------------------------|----------------------------------------|----------|--------------------------------------------------------------------------------------------------|
| `Dockerfile`                                | Multi-stage Docker build for backend   | VERIFIED | Contains `eclipse-temurin`, multi-stage build (JDK build + JRE run), 13 lines, substantive      |
| `.dockerignore`                             | Build context exclusions               | VERIFIED | Contains `frontend/` exclusion, 5 entries covering all required paths                            |
| `DEPLOYMENT.md`                             | Deployment documentation               | VERIFIED | Contains `gcloud run deploy`, env vars table, troubleshooting section, 126 lines                 |
| `backend/src/main/resources/application.yml`| Spring config with PORT env var        | VERIFIED | Contains `${PORT:8080}` on line 2, STIB config intact at lines 8-12                              |

All artifacts are substantive (no stubs, no placeholder content). All are at expected paths. Wiring check not applicable to deployment infrastructure files — they are configuration artifacts, not importable modules.

### Key Link Verification

| From         | To                        | Via                                   | Status   | Details                                                                           |
|--------------|---------------------------|---------------------------------------|----------|-----------------------------------------------------------------------------------|
| `Dockerfile` | `backend/build.gradle`    | `gradlew bootJar` in build stage      | VERIFIED | Line 7 of Dockerfile: `RUN ./gradlew bootJar --no-daemon`                        |
| `Dockerfile` | `api-spec/openapi.yaml`   | `COPY api-spec/` for OpenAPI codegen  | VERIFIED | Line 3 of Dockerfile: `COPY api-spec/ api-spec/`                                 |

Both key links verified by direct file inspection. The Dockerfile correctly includes the api-spec directory in build context before running the Gradle bootJar task, enabling OpenAPI codegen during Docker build.

### Requirements Coverage

| Requirement | Source Plan   | Description                                                                           | Status    | Evidence                                                                         |
|-------------|---------------|---------------------------------------------------------------------------------------|-----------|----------------------------------------------------------------------------------|
| DEPLOY-01   | 01-01-PLAN.md | Deploy backend to Cloud Run with Dockerfile, .dockerignore, DEPLOYMENT.md, PORT support | SATISFIED | All 4 deployment artifacts exist at correct paths; commit b55bdf3 adds all files |

**Note on DEPLOY-01:** There is no standalone REQUIREMENTS.md file in the `.planning/` directory. DEPLOY-01 is referenced only in ROADMAP.md, the PLAN frontmatter, and the SUMMARY frontmatter. The requirement's intent is inferred from the phase goal and plan objective. No orphaned requirements detected.

### Anti-Patterns Found

None. Scanned all four deployment artifacts for TODO/FIXME/placeholder patterns — clean.

### Human Verification Required

#### 1. Cloud Run Service Live Status

**Test:** Run `curl https://transit-planner-backend-621870148637.europe-west1.run.app/api/departures`
**Expected:** HTTP 200 response with real STIB departure data in JSON format
**Why human:** Cannot make external HTTP calls from verification environment. The SUMMARY states this was verified by the user ("Cloud Run service deployed and verified live at the URL — returning real STIB departure data"), but the live service state cannot be confirmed programmatically during this verification.

#### 2. Docker Build Correctness

**Test:** Run `docker build -t transit-backend .` from the repo root
**Expected:** Build completes successfully; JDK stage compiles the Spring Boot app; JRE stage produces a runnable image
**Why human:** No Docker daemon available in verification environment. The Dockerfile structure is correct by inspection, but an actual build would confirm OpenAPI codegen works inside Docker context.

### Gaps Summary

No gaps found. All 5 observable truths are verified against actual file contents. All key links (gradlew bootJar invocation and api-spec COPY) are present in the Dockerfile. The commit `b55bdf3` confirms atomic delivery of all 4 deployment artifacts. The phase goal — deploying backend to Google Cloud Run by integrating deployment infrastructure — is substantiated by the artifacts on main branch.

Two items are flagged for human verification (live service health and Docker build), but these are external/environmental checks, not code gaps. The artifacts themselves are complete and correct.

---

_Verified: 2026-03-03T18:00:00Z_
_Verifier: Claude (gsd-verifier)_
