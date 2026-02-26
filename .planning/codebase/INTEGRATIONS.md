# External Integrations

**Analysis Date:** 2026-02-26

## APIs & External Services

**Authentication (Planned):**
- Clerk - User authentication and identity management
  - Status: Defined in API spec (`api-spec/openapi.yaml`) with bearer JWT scheme but not yet enforced
  - Integration: `components.securitySchemes.bearerAuth` (JWT from Clerk, not validated in MVP)
  - Env var reference: Not yet implemented; would need CLERK_API_KEY or similar

**Transit Data APIs (Planned, Not Yet Integrated):**
- STIB/MIVB (Brussels) - Public transit information API
  - Status: Planned for future phases (per ROADMAP.md)
  - Purpose: Real-time departure times and route information for Brussels

- SNCB/NMBS (Belgium) - Railway API
  - Status: Planned for future phases
  - Purpose: Train schedule and real-time data for Belgium

- TfL (Transport for London) - London transit API
  - Status: Planned for future phases
  - Purpose: Live departure feeds, tube/bus schedules for London

## Data Storage

**Databases:**
- None configured in production. Current implementation uses in-memory storage via `java.util.concurrent.ConcurrentHashMap` in `backend/src/main/java/com/transit/service/RouteService.java`
- PostgreSQL - Listed as planned database in README.md, no ORM configured yet
  - Status: Not implemented (MVP uses in-memory only)
  - When implemented: Will require Spring Data JPA or similar ORM
  - Connection: Would be defined in `application.yml` with SPRING_DATASOURCE_URL env var

**File Storage:**
- None - Application currently has no file upload/storage requirements

**Caching:**
- None - In-memory routes map serves as implicit cache; no distributed cache (Redis, etc.) configured

## Authentication & Identity

**Auth Provider:**
- Clerk - Third-party authentication service
  - Status: Defined in OpenAPI spec but not enforced (MVP stage)
  - Implementation approach: Bearer JWT tokens expected in Authorization header
  - Validation: Currently bypassed; all requests accepted regardless of JWT
  - Future: Controllers will validate Clerk JWT before processing requests

**API Security:**
- Current CORS config: `backend/src/main/java/com/transit/config/CorsConfig.java`
  - Allowed origins: http://localhost:5173 (Vite dev server only)
  - Allowed methods: GET, POST, PUT, DELETE, OPTIONS
  - Allowed headers: All (*)
  - Credentials: Allowed

## Monitoring & Observability

**Error Tracking:**
- None configured - No Sentry, Rollbar, or similar service integrated

**Logs:**
- Standard Spring Boot logging via `logback` (implicit via spring-boot-starter-web)
- Configuration: `backend/src/main/resources/application.yml`
  - Levels: INFO for `com.transit` and `org.springframework.web`
  - Output: Console/stdout (default Spring Boot behavior)

**Metrics:**
- None - No Prometheus, StatsD, or custom metrics collection configured

## CI/CD & Deployment

**Hosting:**
- Not configured - No deployment platform specified
- README.md implies local development only (localhost:8080, localhost:5173)
- Future candidates: Docker containerization, Kubernetes, cloud platforms (AWS, GCP, Azure, Heroku)

**CI Pipeline:**
- None configured - No GitHub Actions, GitLab CI, or similar workflow
- Local development only (run `./gradlew build` and `npm run build` manually)

**Build Artifacts:**
- Backend: `backend/build/` (Gradle build output)
  - JAR file: Not explicitly configured; Spring Boot defaults to creating executable JAR
- Frontend: `frontend/dist/` (Vite production bundle, created by `npm run build`)

## Environment Configuration

**Required env vars:**
- None currently enforced; all configuration hardcoded to localhost
- When implementing external services, will need:
  - CLERK_API_KEY (authentication)
  - SPRING_DATASOURCE_URL (database connection)
  - API keys for STIB, SNCB, TfL transit APIs
  - CORS_ALLOWED_ORIGINS (for production deployment)

**Current Hardcoded Configuration:**
- `api-spec/openapi.yaml` - Server URL hardcoded to http://localhost:8080
- `backend/src/main/resources/application.yml` - Port hardcoded to 8080
- `frontend/vite.config.ts` - API proxy hardcoded to http://localhost:8080
- `backend/src/main/java/com/transit/config/CorsConfig.java` - Origin hardcoded to http://localhost:5173

**Secrets location:**
- Not yet configured - No .env file or secrets vault in use
- When implemented: Should use environment variables (12-factor app pattern)
- Production recommendation: Use platform-specific secrets management (AWS Secrets Manager, GitHub Secrets, etc.)

## Webhooks & Callbacks

**Incoming:**
- None configured - Application receives no external webhooks

**Outgoing:**
- None configured - Application makes no webhook callbacks to external systems

## API Integration Details

**Backend HTTP Client:**
- Current: None - No REST client or HTTP library explicitly imported for external API calls
- When implementing transit APIs: Consider Spring's RestTemplate or WebClient
- Generated API dependencies: Gradle manages all transitive dependencies via Spring Boot BOM

**Frontend API Client:**
- Custom Axios client: `frontend/src/client.ts`
  - Implements basic fetch-based HTTP client
  - Supports: Request config (method, data, signal, headers), response handling, error propagation
  - Generated clients: Kubb generates Axios-based clients in `frontend/src/gen/` with React Query integration

**Client Generation:**
- Tool: Kubb 4.12.8 with plugins for React Query, TypeScript, and Axios
- Trigger: `npm run generate` command in `frontend/package.json`
- Output: `frontend/src/gen/` (hooks, types, clients)
- Update frequency: Regenerate after `api-spec/openapi.yaml` changes

---

*Integration audit: 2026-02-26*
