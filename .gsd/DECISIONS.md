# Decisions

## D001 — Departures first, auth later
- Scope: architecture
- Decision: Ship real-time departures before adding authentication
- Choice: Core value (departure times) ships in v1.0, Clerk auth deferred
- Rationale: The reason the app exists is seeing departure times, not logging in
- Made by: collaborative
- When: M001

## D002 — Hardcoded routes for v1
- Scope: architecture
- Decision: How to handle route data in v1
- Choice: Hardcode Woest line 51 + Pannenhuis line 6 as Java constants
- Rationale: Fastest path to real departures — no CRUD, no persistence needed
- Made by: collaborative
- When: M001

## D003 — Skip full DDD for v1
- Scope: architecture
- Decision: Backend architecture complexity level
- Choice: Controller/service/client pattern instead of full DDD hexagonal
- Rationale: Overkill for 2 phases of work — evolve when complexity demands it
- Made by: collaborative
- When: M001

## D004 — Custom Jackson deserializer for STIB
- Scope: library
- Decision: How to parse STIB's nested JSON-string-inside-JSON response
- Choice: Custom StdDeserializer with its own ObjectMapper for passingtimes field
- Rationale: STIB returns JSON-encoded strings inside JSON — standard mapping can't handle this
- Made by: agent
- When: M001/S01

## D005 — React Query for auto-refresh
- Scope: library
- Decision: How to implement 30-second auto-refresh of departure data
- Choice: React Query useQuery with refetchInterval: 30_000
- Rationale: Built-in refetchInterval, caching, stale data handling — minimal code for complex behavior
- Made by: agent
- When: M001/S02

## D006 — Plain fetch over generated client
- Scope: architecture
- Decision: Whether frontend should use Kubb-generated API client for data fetching
- Choice: Plain fetch in infrastructure adapter
- Rationale: Clean architecture — infrastructure adapter owns HTTP concerns, no framework coupling
- Made by: agent
- When: M001/S02

## D007 — Contract-first API with OpenAPI 3.0
- Scope: architecture
- Decision: API design approach
- Choice: OpenAPI spec is source of truth, generates both Java interfaces and TypeScript types
- Rationale: Type safety across the wire with zero manual sync — caught zero type mismatches at boundary
- Made by: collaborative
- When: M001

## D008 — Clock injection for testable time
- Scope: architecture
- Decision: How to handle time-dependent logic in DeparturesService
- Choice: Inject java.time.Clock as Spring @Bean, use fixed Clock in tests
- Rationale: Deterministic minutesUntilArrival assertions without time-dependent flakiness
- Made by: agent
- When: M001/S01

## D009 — Dockerfile at repo root
- Scope: architecture
- Decision: Where to place Dockerfile
- Choice: Repo root, not backend/
- Rationale: Gradle build needs ../api-spec/openapi.yaml for OpenAPI codegen — must be in build context
- Made by: agent
- When: M002/S01

## D010 — Multi-stage Docker build
- Scope: architecture
- Decision: Docker image build strategy
- Choice: eclipse-temurin:25-jdk for build, eclipse-temurin:25-jre for run
- Rationale: Smaller runtime image — JRE stage doesn't include build tools
- Made by: agent
- When: M002/S01

## D011 — PORT env var with fallback
- Scope: architecture
- Decision: How to handle server port for Cloud Run vs local dev
- Choice: ${PORT:8080} — Cloud Run injects PORT, local dev uses 8080
- Rationale: Single config works for both environments without changes
- Made by: agent
- When: M002/S01
