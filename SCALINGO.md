# Deploying the Backend to Scalingo

Alternative backend host to Google Cloud Run (see `DEPLOYMENT.md`). The Spring Boot
app is built and run on Scalingo via its Gradle buildpack. Both targets can coexist:
Cloud Run deploys via GitHub Actions on push to `main`; Scalingo deploys via
`git push scalingo main`.

## Monorepo layout — the key setting

This repo is a monorepo: the Gradle project lives in `backend/`, not at the root, and
the build reads the OpenAPI spec from the sibling `../api-spec/openapi.yaml` at build
time (only — the boot jar is self-contained at runtime).

Scalingo must be told where the app lives (app name: `transports`, region `osc-fr1`):

```bash
scalingo --app transports env-set PROJECT_DIR=backend
```

With `PROJECT_DIR=backend`, Scalingo `cd`s into `backend/`, detects Gradle from the
committed wrapper there, and runs `./gradlew stage`. The full repo is checked out
during the build, so `../api-spec` resolves; only `backend/` is shipped to the runtime
image (which is all the jar needs).

## Files added for Scalingo (all under `backend/`)

| File | Purpose |
|------|---------|
| `backend/system.properties` | Pins the JDK: `java.runtime.version=25` |
| `backend/Procfile` | Start command: `web: java -Dserver.port=$PORT $JAVA_OPTS -jar build/libs/*.jar` |
| `backend/build.gradle` | Added a `stage` task (runs `bootJar`) and disabled the plain jar so `build/libs/*.jar` is unambiguous |

`server.port=${PORT:8080}` was already set in `application.yml` (from the Cloud Run work),
so Scalingo's injected `$PORT` is honored with no change.

> ⚠️ **Java 25:** the JDK is pinned to 25. If Scalingo's buildpack does not yet ship
> Azul Zulu 25, the build fails at `Installing Azul Zulu OpenJDK` — downgrade the
> toolchain in `backend/build.gradle` and `system.properties` to 21 if so.

## Environment variables to set on Scalingo

| Variable | Value | Required |
|----------|-------|----------|
| `PROJECT_DIR` | `backend` | Yes (without it, detection fails at repo root) |
| `STIB_API_KEY` | STIB/MIVB Open Data API key | Yes |
| `CORS_ALLOWED_ORIGINS` | e.g. `https://transit-planner-app.netlify.app` | Yes |

## Deploy steps

Deploys to the existing `transports` app. The git remote is already configured:
`scalingo  git@ssh.osc-fr1.scalingo.com:transports.git`.

```bash
# 1. Configure env (PROJECT_DIR before first deploy so detection succeeds).
#    Set via dashboard (Environment) or the CLI:
scalingo --app transports env-set PROJECT_DIR=backend
scalingo --app transports env-set STIB_API_KEY=...
scalingo --app transports env-set CORS_ALLOWED_ORIGINS=https://transit-planner-app.netlify.app

# 2. Deploy
git push scalingo main
```

A successful build logs `./gradlew stage` → `BUILD SUCCESSFUL` →
`<-- https://transit-planner-backend.osc-fr1.scalingo.io -->`.

## Local sanity check (mirrors what Scalingo runs)

```bash
cd backend
./gradlew clean stage          # must produce exactly one build/libs/*.jar
java -Dserver.port=8080 -jar build/libs/*.jar
curl localhost:8080/api/departures
```

## Switching the frontend over

The Netlify frontend reads its backend URL from the `VITE_API_BASE_URL` build env var
(see `frontend/src/infrastructure/api/api-dashboard-service.ts`). To point it at
Scalingo instead of Cloud Run, update `VITE_API_BASE_URL` in the Netlify dashboard to
the Scalingo URL and redeploy the frontend.
