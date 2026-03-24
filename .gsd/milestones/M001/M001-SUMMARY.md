---
id: M001
provides:
  - Real-time STIB departures dashboard with 30s auto-refresh
  - Contract-first API (OpenAPI 3.0) with generated code on both sides
  - Clean architecture frontend (entities/use-cases/infrastructure/presentation)
  - Backend controller/service/client pattern with custom STIB JSON deserializer
key_decisions:
  - Hardcoded routes for v1 (Woest line 51, Pannenhuis line 6)
  - Plain fetch over generated client in frontend infrastructure adapter
  - React Query for auto-refresh with refetchInterval
  - Clock injection for testable time-dependent logic
  - Custom Jackson deserializer for STIB nested JSON strings
patterns_established:
  - Contract-first OpenAPI driving code generation on both Java and TypeScript
  - Honeycomb testing — WireMock for service tests, MockMvc + stubbed service for integration
  - React Query isLoading && !data pattern for skeleton on initial load only
  - Clean architecture DI via React Context ServiceProvider
observability_surfaces:
  - none (v1 — add observability when complexity warrants)
requirement_outcomes:
  - id: R001
    from_status: active
    to_status: validated
    proof: Live STIB API returns departure data via GET /api/departures
  - id: R002
    from_status: active
    to_status: validated
    proof: DeparturesService provides lineNumber, direction, minutesUntilArrival
  - id: R003
    from_status: active
    to_status: validated
    proof: React Query refetchInterval 30_000 — verified in browser
  - id: R004
    from_status: active
    to_status: validated
    proof: SkeletonCard shown on isLoading && !data
  - id: R005
    from_status: active
    to_status: validated
    proof: ErrorCard with retry button on error && !data
  - id: R006
    from_status: active
    to_status: validated
    proof: FavoriteRouteCard → LineDepartures → DeparturePill component hierarchy
duration: 40 days (2026-01-22 → 2026-03-02)
verification_result: passed
completed_at: 2026-03-02
---

# M001: v1.0 MVP

**Real-time STIB departures dashboard with contract-first API, 30s auto-refresh, and robust loading/error/staleness UX states**

## What Happened

Two slices delivered the core value end-to-end. S01 (Backend STIB Integration) built the departures API: OpenAPI contract defining GET /api/departures, a STIB HTTP client using java.net.http.HttpClient with custom Jackson deserializer for the passingtimes-as-JSON-string quirk, and DeparturesService with hardcoded Woest/Pannenhuis routes filtered by commute direction. S02 (Frontend Live Integration) wired the dashboard to real data: remodeled frontend types to match the API shape, created an infrastructure adapter using plain fetch, added React Query with 30s auto-refresh, then layered on SkeletonCard, ErrorCard with retry, and StalenessWarning (2-minute threshold). Mock data service was deleted entirely.

1,186 LOC TypeScript + 526 LOC Java shipped across 4 tasks in 2 slices.

## Cross-Slice Verification

- Backend: WireMock tests for STIB JSON deserialization, Mockito unit tests for DeparturesService, MockMvc integration tests
- Frontend: Vitest tests for GetDepartures use case
- Phase 2 verification report: 7/7 observable truths verified programmatically, 5 human verification items (live browser testing)
- Milestone audit: 6/6 requirements satisfied, 7/7 integration checks passed, 0 broken flows

## Requirement Changes

- R001: active → validated — Live STIB API integration confirmed
- R002: active → validated — Departure details verified in API response
- R003: active → validated — 30s auto-refresh confirmed via React Query
- R004: active → validated — Skeleton loading verified
- R005: active → validated — Error card with retry verified
- R006: active → validated — Dashboard layout verified

## Forward Intelligence

### What the next milestone should know
- STIB API key is required as STIB_API_KEY env var — app fails fast at startup if missing
- Frontend hardcodes localhost:8080 in api-dashboard-service.ts — needs config for non-local deployments
- Routes are hardcoded as Java constants in DeparturesService — no persistence layer exists

### What's fragile
- api-dashboard-service.ts hardcoded base URL — breaks when backend isn't on localhost:8080
- StalenessWarning may briefly flash on mount when dataUpdatedAt is 0 — cosmetic edge case

### Authoritative diagnostics
- `curl http://localhost:8080/api/departures | jq .` — fastest way to verify backend is serving data
- React Query devtools (if added) would show query state, but not installed in v1

### What assumptions changed
- STIB API documentation was inaccurate — real response had nested JSON strings requiring custom deserializer
