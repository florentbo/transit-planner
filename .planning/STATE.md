---
gsd_state_version: 1.0
milestone: v1.0
milestone_name: milestone
status: in_progress
last_updated: "2026-03-02T22:24:00Z"
progress:
  total_phases: 2
  completed_phases: 1
  total_plans: 4
  completed_plans: 3
---

# Project State

## Project Reference

See: .planning/PROJECT.md (updated 2026-02-26)

**Core value:** Open the app -> instantly see real-time departures for your commute routes
**Current focus:** Phase 2 - Frontend Live Integration

## Current Position

Phase: 2 of 2 (Frontend Live Integration)
Plan: 2 of 2 in current phase
Status: In progress
Last activity: 2026-03-02 — Completed plan 02-01 (wire frontend to real departures API)

Progress: [███████░░░] 75%

## Performance Metrics

**Velocity:**
- Total plans completed: 3
- Average duration: 3 min
- Total execution time: 10 min

**By Phase:**

| Phase | Plans | Total | Avg/Plan |
|-------|-------|-------|----------|
| 01 - Backend STIB Integration | 2 | 8 min | 4 min |
| 02 - Frontend Live Integration | 1 | 3 min | 3 min |

**Recent Trend:**
- Last 5 plans: 3 min
- Trend: baseline

*Updated after each plan completion*
| Phase 01-backend-stib-integration P02 | 2 | 1 tasks | 4 files |
| Phase 02-frontend-live-integration P01 | 3 | 2 tasks | 11 files |

## Accumulated Context

### Decisions

Decisions are logged in PROJECT.md Key Decisions table.
Recent decisions affecting current work:

- Port STIB integration logic from old backend, not the architecture or naming
- Use contract-first: define departures endpoint in openapi.yaml before implementing
- Hardcoded routes for v1 (Woest/line 51, Pannenhuis/line 6) — no route CRUD
- Keep controller/service/client structure, skip full DDD layers
- [01-01] Use java.net.http.HttpClient with 5-second timeout for STIB API calls
- [01-01] PassingTimesDeserializer uses its own ObjectMapper to avoid shared state
- [01-01] ADJUST_DATES_TO_CONTEXT_TIME_ZONE=false preserves +01:00 offset from STIB data
- [01-01] StibApiException created as dedicated RuntimeException for HTTP errors and timeouts
- [Phase 01-02]: Clock injected as Spring @Bean in CorsConfig for testable time logic in DeparturesService
- [Phase 01-02]: ExceptionHandler on DeparturesApiController maps ResponseStatusException(BAD_GATEWAY) to structured ErrorResponse
- [Phase 01-02]: Direction filter uses equalsIgnoreCase on destination.fr() against commute route direction string
- [02-01] Plain fetch in infrastructure adapter (not kubb-generated client) — adapter owns HTTP concerns
- [02-01] Hardcode userName=Florent and city=Brussels in presentation layer — auth is v2 concern
- [02-01] IDashboardService port is async Promise<DeparturesData> — infrastructure adapter handles error throwing

### Pending Todos

None yet.

### Blockers/Concerns

- STIB API key must be available as environment variable before end-to-end testing

## Session Continuity

Last session: 2026-03-02
Stopped at: Completed 02-01-PLAN.md (wire frontend to real departures API)
Resume file: .planning/phases/02-frontend-live-integration/02-02-PLAN.md
