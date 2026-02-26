# Codebase Concerns

**Analysis Date:** 2026-02-26

## Tech Debt

**Incomplete Architecture Implementation:**
- Issue: Design is defined in `ARCHITECTURE-DECISIONS.md` but codebase does not yet implement it. Current backend has minimal structure—only `RouteService` and `RoutesApiController` with in-memory storage. No domain layer (aggregates, value objects), no application layer (use cases), no proper port/adapter pattern.
- Files: `backend/src/main/java/com/transit/service/RouteService.java`, `backend/src/main/java/com/transit/controller/RoutesApiController.java`
- Impact: High. Backend will require significant refactoring as features are added. Current code shortcuts DDD patterns and will become harder to extend with proper domain logic (invariants, entities, value objects). Frontend similarly uses mock data only.
- Fix approach: Phase implementation incrementally: (1) Establish backend domain layer with value objects and aggregates (2) Implement application layer with use cases and ports (3) Refactor RouteService into use cases; (4) Add infrastructure adapters layer. Use ArchUnit tests to enforce dependency rules as code is added.

**In-Memory Storage Only (No Database):**
- Issue: Routes stored in `ConcurrentHashMap` in `RouteService.routes` field. Routes disappear on server restart. No persistence layer implemented.
- Files: `backend/src/main/java/com/transit/service/RouteService.java` (line 17)
- Impact: High. MVP requires persistent storage. Feature 2 (Roadmap) blocks all downstream features until database is implemented. Current in-memory approach cannot support multi-user isolation or scale testing.
- Fix approach: Implement JPA repository adapters (Feature 2 roadmap phase). Add PostgreSQL testcontainers for integration tests. Create Flyway migration scripts. Map JPA entities to domain aggregates using mappers.

**No Authentication Enforcement:**
- Issue: OpenAPI spec defines `bearerAuth` (JWT from Clerk) in security scheme (line 10-75 in `api-spec/openapi.yaml`), but implementation comment says "not validated in MVP" (line 75). Controllers do not extract or validate JWT tokens. No user context filtering on routes.
- Files: `api-spec/openapi.yaml` (line 75), `backend/src/main/java/com/transit/controller/RoutesApiController.java`
- Impact: High. All routes visible to all clients. Feature 1 (User Accounts) cannot complete without this. Data is not user-scoped.
- Fix approach: Implement Clerk authentication adapter in infrastructure layer. Extract JWT from Authorization header in all controllers. Map Clerk external ID to internal User aggregate. Filter routes by authenticated user in all queries.

**Missing Domain Model Implementation:**
- Issue: Aggregates, entities, and value objects defined in ARCHITECTURE-DECISIONS.md (sections 2.1-2.4) are not implemented. No User aggregate, no SavedRoute aggregate, no TransportOption entity. No value objects (Location, Email, Stop, Line, Direction). Current implementation uses API DTOs directly as domain model.
- Files: Architecture defined in `ARCHITECTURE-DECISIONS.md` (lines 27-118), but no corresponding code in `backend/src/main/java/com/transit/`
- Impact: High. Cannot enforce business invariants. Route validation missing. Cannot add complex transit features (Feature 4+) without domain model to represent TransportOption, Stop, Line relationships.
- Fix approach: Implement domain layer package structure (`com.transit.domain.user`, `com.transit.domain.route`, `com.transit.domain.shared`). Create immutable value objects first (Location, Email, Stop, Line, Direction). Then aggregates with invariant enforcement. Write pure unit tests for domain logic. Keep domain free of Spring/framework dependencies.

**No Repository Port Implementations:**
- Issue: `ARCHITECTURE-DECISIONS.md` defines `UserRepository` and `SavedRouteRepository` as ports (interfaces) in domain layer (lines 43-47, 74-78), but neither interface nor implementation exists. No separation between domain ports and infrastructure adapters.
- Files: Not implemented; should exist at `backend/src/main/java/com/transit/domain/user/UserRepository.java` and `backend/src/main/java/com/transit/domain/route/SavedRouteRepository.java`
- Impact: Medium. Code does not follow hexagonal architecture. Adding new data sources or swapping persistence layer would be difficult. Unit tests cannot easily mock persistence.
- Fix approach: Define repository interfaces in domain layer. Create JPA adapter implementations in infrastructure layer. Use mappers to convert between JPA entities (persistence model) and domain aggregates (business model).

## Known Bugs

**No Routes Deletion or Update Capability:**
- Symptoms: Only POST (create) and GET (list) endpoints exist. No PUT/DELETE endpoints. Users cannot edit or remove saved routes.
- Files: `backend/src/main/java/com/transit/controller/RoutesApiController.java`, `backend/src/main/java/com/transit/service/RouteService.java`, `api-spec/openapi.yaml`
- Trigger: User attempts to edit or delete a route
- Workaround: None. Must restart server to clear in-memory routes.

**Route Creation Lacks Validation:**
- Symptoms: No input validation on SavedRouteRequest fields (name, origin, destination). Empty strings accepted. Fields exceed max length constraints defined in OpenAPI schema (minLength: 1, maxLength: 100-200 at lines 89-102).
- Files: `backend/src/main/java/com/transit/service/RouteService.java`, `backend/src/main/java/com/transit/controller/RoutesApiController.java`
- Trigger: POST to `/api/routes` with empty name or oversized destination
- Workaround: Frontend-side validation only (unreliable).

**Frontend Uses Hardcoded Mock Data:**
- Symptoms: `App.tsx` (line 7) hardcodes mock dashboard service. No real API calls. `mockDashboardService.ts` returns static data independent of backend.
- Files: `frontend/src/App.tsx`, `frontend/src/infrastructure/mock/mock-dashboard-service.ts`
- Trigger: Start frontend—always shows "Florent" from Brussels on Metro 1
- Workaround: None for real data; frontend cannot currently integrate with backend.

## Security Considerations

**CORS Allows Any Headers:**
- Risk: Wildcard `allowedHeaders("*")` in CorsConfig accepts any header, including potentially malicious ones. Combined with allowCredentials(true), this violates secure CORS practice.
- Files: `backend/src/main/java/com/transit/config/CorsConfig.java` (line 19)
- Current mitigation: Frontend origin restricted to localhost:5173 only
- Recommendations: Replace `allowedHeaders("*")` with explicit whitelist: `allowedHeaders("Content-Type", "Authorization")`. Add CSP headers. In production, use environment variable for allowed origins instead of hardcoded localhost.

**No Input Validation on API Requests:**
- Risk: SavedRouteRequest fields not validated at controller level. Could accept null, extremely long, or special-character-laden values. No protection against injection or DoS.
- Files: `backend/src/main/java/com/transit/controller/RoutesApiController.java`, `backend/src/main/java/com/transit/service/RouteService.java`
- Current mitigation: OpenAPI schema defines constraints but they are not enforced in code
- Recommendations: Add `@Valid` annotation to request parameters. Use custom validators for domain invariants (email format, coordinate ranges, transit provider enums). Return 400 Bad Request with constraint details on validation failure.

**JWT Token Not Validated:**
- Risk: Bearer token in Authorization header not validated. Any token string accepted. Cannot identify user or enforce per-user data access.
- Files: `backend/src/main/java/com/transit/controller/RoutesApiController.java`
- Current mitigation: OpenAPI spec documents that validation is skipped in MVP
- Recommendations: Implement ClerkAuthAdapter to validate JWT signature, check expiration, extract user identity. Add Spring Security filter to apply to all `/api/**` endpoints. Return 401 Unauthorized if token missing or invalid.

**No Error Response Standardization:**
- Risk: Unhandled exceptions may leak stack traces or internal details to client. No consistent error response format across endpoints.
- Files: Controllers and service lack try-catch and error mapping
- Current mitigation: None
- Recommendations: Create `@ControllerAdvice` global exception handler. Map domain exceptions (InvalidEmailException, RouteNotFoundException) to HTTP status codes and standardized ErrorResponse (as defined in OpenAPI schema, lines 136-150).

**Frontend Exposes Hardcoded User Data:**
- Risk: Mock dashboard shows "Florent" username and specific Brussels routes. Could reveal dev data patterns to users.
- Files: `frontend/src/infrastructure/mock/mock-dashboard-service.ts`
- Current mitigation: Test data only; not connected to real backend
- Recommendations: Once backend auth is implemented, fetch current user context from Clerk SDK. Remove all hardcoded names and routes.

## Performance Bottlenecks

**In-Memory Map Linear Scan on List All Routes:**
- Problem: `RouteService.getAllRoutes()` returns all routes as a new ArrayList. No pagination, no filtering. With thousands of routes, memory usage and response time grow linearly.
- Files: `backend/src/main/java/com/transit/service/RouteService.java` (line 32-34)
- Cause: Simple in-memory implementation; no database query optimization
- Improvement path: When database is added, implement `findByUserId(UserId)` with pagination (limit/offset). Add index on user_id in saved_routes table. Return PageResponse with total count and hasnext flag.

**No Caching on Frequently-Accessed Data:**
- Problem: Real-time departures (Feature 6) will call transit APIs repeatedly for same stop+line. No cache layer.
- Files: Not yet implemented (departures endpoint missing)
- Cause: Architecture notes (section 8.3) explicitly defers caching to post-MVP
- Improvement path: Add Redis for short-lived departures cache (TTL 30-60 seconds). Cache user routes in-process with Caffeine. Implement cache invalidation strategy on route updates.

**No Database Connection Pooling:**
- Problem: Once JDBC is added, default connection pool settings may be too small (HikariCP default 10 connections) for concurrent load.
- Files: Not yet implemented (in-memory storage)
- Cause: MVP uses in-memory storage
- Improvement path: Configure HikariCP pool size in `application.yml`. Set maximumPoolSize based on expected concurrent users. Monitor connection utilization. Add connection pool metrics to production observability.

## Fragile Areas

**RouteService Has No Isolation Between Tests or Requests:**
- Files: `backend/src/main/java/com/transit/service/RouteService.java`
- Why fragile: ConcurrentHashMap is shared across all HTTP requests. Test execution order affects results. If parallel tests run, one test's routes appear in another's list. No transaction boundaries or test isolation.
- Safe modification: Replace ConcurrentHashMap with repository interface. Inject repository via constructor. Use Spring @Transactional to isolate test transactions. This is part of Feature 2 refactoring.
- Test coverage: No unit tests currently exist for RouteService.

**Frontend Components Tightly Coupled to Mock Data:**
- Files: `frontend/src/App.tsx`, `frontend/src/presentation/pages/DashboardPage.tsx`, `frontend/src/infrastructure/mock/mock-dashboard-service.ts`
- Why fragile: Components expect exact data structure from mockDashboardService. When backend API is wired, data format may differ (e.g., API returns `SavedRouteResponse` but mock uses custom `FavoriteRoute` type). No adapter layer between generated API client and components.
- Safe modification: Create infrastructure adapters that map API responses to domain entity types. Inject via React Context/DI. Ensure components depend on stable entity interfaces, not API DTOs.
- Test coverage: Dashboard page has one integration test (`get-dashboard.test.ts`), but tests against mock service only. No tests with real API response structure.

**No Error Boundary or Fallback UI:**
- Files: `frontend/src/App.tsx`, `frontend/src/presentation/pages/DashboardPage.tsx`
- Why fragile: If dashboard service throws error, app crashes with no fallback. No try-catch or error boundary. Users see blank/broken page.
- Safe modification: Add React Error Boundary. Implement error handling in use cases (return Result<T, Error> type). Display toast notifications for recoverable errors. Show error page for fatal errors.
- Test coverage: No tests for error scenarios.

## Scaling Limits

**In-Memory Storage Cannot Scale Beyond Single Server:**
- Current capacity: Limited by heap memory (~4GB default JVM heap = ~100K routes at 40KB per route object)
- Limit: With persistent user data, heap exhaustion at production load. No load balancing across servers possible (data not shared).
- Scaling path: Implement PostgreSQL (Feature 2). Use stateless backend instances. Routes stored centrally, any instance can serve requests.

**Frontend Generated API Client Not Optimized for Large Lists:**
- Current capacity: Generated hooks (useListRoutes, useCreateRoute) have no pagination, caching, or optimistic updates.
- Limit: If user has 1000+ routes, JSON response is large. Every re-render refetches entire list.
- Scaling path: Implement cursor-based pagination in API. Use TanStack Query (already installed via Kubb) for caching and prefetching. Add optimistic updates for create/delete.

**No Rate Limiting on Routes API:**
- Current capacity: Any client can POST unlimited routes, consuming memory.
- Limit: Malicious or accidental DoS attack (e.g., script creates 1M routes) exhausts memory.
- Scaling path: Add Spring Cloud Gateway rate limiter. Limit per-user or per-IP. Return 429 Too Many Requests when exceeded.

## Dependencies at Risk

**No Spring Data JPA Dependency (Yet):**
- Risk: Once database is added (Feature 2), JPA version must be chosen and integrated. Spring Boot 4.0 requires JPA 3.x. Must align with Jakarta EE transition.
- Current state: `build.gradle` has no JPA dependencies yet
- Impact: Feature 2 implementation will require gradle config update, migration script framework (Flyway vs Liquibase choice)
- Migration plan: Add `org.springframework.boot:spring-boot-starter-data-jpa` to dependencies. Use Flyway for versioned migrations. Test with testcontainers PostgreSQL.

**Jakarta.validation Imported but Not Used:**
- Risk: `jakarta.validation:jakarta.validation-api:3.1.0` in dependencies but no @Valid, @NotBlank, @Pattern annotations in code. Dead dependency.
- Files: `backend/build.gradle` (line 24)
- Impact: Low. No functional impact, but adds confusion.
- Migration plan: Once input validation is implemented, annotations will be used. Remove from dependencies if validation framework changes.

**OpenAPI Generator v7.10.0 Pinned (2025 Release):**
- Risk: Major version updates may change code generation patterns. Configuration in `openApiGenerate` task may become incompatible.
- Files: `backend/build.gradle` (line 2)
- Impact: Low for now. Matters when upgrading.
- Migration plan: Monitor OpenAPI Generator releases. Test upgrade on feature branch before applying. Generated code review on each upgrade.

**Frontend Kubb Generator Has High Coupling:**
- Risk: `kubb.config.ts` generates hooks (useListRoutes, useCreateRoute) based on OpenAPI spec. If hook pattern changes or spec schema names change, generated code breaks.
- Files: `frontend/kubb.config.ts`, `frontend/src/gen/hooks/`
- Impact: Medium. Route creation currently works with generated hooks, but tight coupling makes refactoring harder.
- Migration plan: Wrap generated hooks in domain-specific hooks (e.g., `useSavedRoutes` wraps `useListRoutes`). Infrastructure adapter layer isolates components from generated code changes.

## Missing Critical Features

**No Real Transit API Integration:**
- Problem: Feature 6 (Live Departures) requires STIB/SNCB/TfL adapters, but `RealTimeTransitProvider` port is defined in architecture but not implemented.
- Blocks: Features 6, 7, 8 cannot be completed. Without departures, app has no unique value proposition over a spreadsheet.
- Scope: Need to implement StibTransitAdapter, SncbTransitAdapter, TflTransitAdapter in `backend/src/main/java/com/transit/infrastructure/adapters/outbound/transit/`
- API keys: Need to obtain and secure STIB, SNCB, TfL API keys. Current `.env` approach must be reviewed for secrets management.

**No User Account System:**
- Problem: Feature 1 (User Accounts) blocks Features 2-8. Clerk integration defined but not implemented.
- Blocks: Routes cannot be user-scoped. Multi-user scenarios untested.
- Scope: Need ClerkAuthAdapter, User aggregate, UserRepository, JWT validation, test Clerk webhook integration
- Timeline: Feature 1 is first in roadmap; should be prioritized before persistence.

**No Route Edit/Delete Operations:**
- Problem: Feature 3 (Route Management) requires PUT/DELETE endpoints, but only POST/GET exist.
- Blocks: Users stuck with routes forever (except server restart).
- Scope: Add endpoints to OpenAPI spec, regenerate, implement use cases (UpdateSavedRoute, DeleteRoute), repository methods.
- Dependencies: Requires Feature 2 (persistence) first.

## Test Coverage Gaps

**No Backend Unit Tests:**
- What's not tested: RouteService logic, controller endpoint behavior, model validation
- Files: `backend/src/main/java/com/transit/service/RouteService.java`, `backend/src/main/java/com/transit/controller/RoutesApiController.java` have no corresponding test files
- Risk: Refactoring RouteService (when moving to architecture) may break functionality silently. No confidence in model behavior.
- Priority: High. Before Feature 2 refactoring, establish test suite for existing endpoints (POST /routes, GET /routes).

**No API Integration Tests:**
- What's not tested: Full request/response cycle, HTTP status codes, error responses, CORS behavior
- Files: No test files in `backend/src/test/`
- Risk: Breaking changes to API contract go undetected. Frontend integration may fail due to response format mismatches.
- Priority: High. Create integration tests using Spring Boot TestClient or MockMvc. Verify each OpenAPI endpoint returns declared response types.

**No Validation Error Tests:**
- What's not tested: Invalid input handling (empty name, oversized destination, null values)
- Files: Controller and service do not test invalid inputs
- Risk: Malformed requests may crash server or leak unexpected responses.
- Priority: Medium. Create parameterized tests for constraint violations once validation is added.

**Frontend Component Tests Missing Error Scenarios:**
- What's not tested: Dashboard page errors (service throws, network failure), loading states, empty states
- Files: `frontend/src/presentation/pages/DashboardPage.tsx` has no test file
- Risk: UI may break in production when service fails. Users see blank page instead of helpful error message.
- Priority: Medium. Add tests for error boundaries, fallback UI, retry logic once DashboardPage is wired to real API.

**No E2E Test Suite:**
- What's not tested: Create route → view departures, user login → see private routes, edit route name
- Files: No E2E tests (Playwright, Cypress, etc.)
- Risk: Critical user journeys may break between backend and frontend. Regressions discovered by users, not tests.
- Priority: Medium. Implement after Features 1-3 complete (auth, persistence, CRUD). Use Playwright for cross-browser E2E tests.

---

*Concerns audit: 2026-02-26*
