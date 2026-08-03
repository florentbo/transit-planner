# Deploying the Backend to Scalingo

> ⚠️ **Parked fallback — no longer serving the frontend.** The backend now runs on
> **Google Cloud Run** (see `DEPLOYMENT.md`), reached at `https://transport-back.bonamis.be`,
> which is what Netlify's `VITE_API_BASE_URL` points at.
>
> The Scalingo app is still deployed and still auto-deploys on push to `main`, so it stays
> current as a hot standby on its free tier.
>
> To fall back to it: add `transport-back.bonamis.be` as a custom domain on the Scalingo
> app **first** (so Scalingo provisions a cert for that hostname), then repoint the
> `transport-back` CNAME at OVH. Repointing DNS alone breaks TLS — Scalingo would serve
> a `*.osc-fr1.scalingo.io` certificate for a hostname it doesn't know. No frontend
> rebuild is needed either way, which is the point of the custom domain.

The Spring Boot app is built and run on Scalingo via its Gradle buildpack, and
**auto-deploys on every push to `origin/main`**.

- **Direct URL:** `https://transports.osc-fr1.scalingo.io`
- **App / region:** `transports` / `osc-fr1`
- **Frontend:** no longer points here.

## Auto-deploy (the normal flow)

The Scalingo app is linked to the GitHub repo with auto-deploy on `main`:

```
git push origin main
   └─► GitHub webhook ─► Scalingo pulls source ─► ./gradlew stage ─► ships + boots
```

The build runs **on Scalingo's servers**, not locally and not on GitHub. Your
`git push origin main` returns immediately; watch the background build with:

```bash
scalingo --app transports deployment-follow
```

## Monorepo layout — the key setting

This repo is a monorepo: the Gradle project lives in `backend/`, not at the root, and
the build reads the OpenAPI spec from the sibling `../api-spec/openapi.yaml` at build
time (only — the boot jar is self-contained at runtime).

Scalingo is told where the app lives via the `PROJECT_DIR` env var:

```bash
scalingo --app transports env-set PROJECT_DIR=backend
```

With `PROJECT_DIR=backend`, Scalingo `cd`s into `backend/`, detects Gradle from the
committed wrapper there, and runs `./gradlew stage`. The full repo is checked out
during the build, so `../api-spec` resolves; only `backend/` is shipped to the runtime
image (which is all the jar needs).

## Files that make this work (all under `backend/`)

| File | Purpose |
|------|---------|
| `backend/system.properties` | Pins the JDK: `java.runtime.version=25` (Scalingo ships Azul Zulu 25) |
| `backend/Procfile` | Start command: `web: java -Dserver.port=$PORT $JAVA_OPTS -jar build/libs/*.jar` |
| `backend/build.gradle` | A `stage` task (runs `bootJar`) and the plain jar disabled so `build/libs/*.jar` is unambiguous |

`server.port=${PORT:8080}` in `application.yml` honors Scalingo's injected `$PORT`.

## Environment variables (already set on the app)

| Variable | Value | Notes |
|----------|-------|-------|
| `PROJECT_DIR` | `backend` | Required, or detection fails at repo root |
| `STIB_API_KEY` | *(secret)* | STIB/MIVB Open Data API key |
| `CORS_ALLOWED_ORIGINS` | `https://transit-planner-app.netlify.app` | Allows the Netlify frontend |

To change one: `scalingo --app transports env-set NAME=value` then
`scalingo --app transports restart`.

## Manual deploy (fallback)

The `scalingo` git remote is configured
(`git@ssh.osc-fr1.scalingo.com:transports.git`). If you ever need to deploy without
going through GitHub:

```bash
git push scalingo main
```

This streams the remote build live and blocks until it finishes (~1 min).

## Local sanity check (mirrors what Scalingo runs)

```bash
cd backend
./gradlew clean stage          # must produce exactly one build/libs/*.jar
java -Dserver.port=8080 -jar build/libs/*.jar
curl localhost:8080/api/departures
```

## Troubleshooting

- **Detection fails / builds at repo root:** `PROJECT_DIR=backend` is missing.
- **Build fails at `Installing Azul Zulu OpenJDK`:** the pinned JDK isn't available —
  lower `java.runtime.version` (and the Gradle toolchain in `build.gradle`).
- **Frontend CORS errors:** `CORS_ALLOWED_ORIGINS` must include the Netlify origin.
