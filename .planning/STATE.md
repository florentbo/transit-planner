# Project State

## Project Reference

See: .planning/PROJECT.md (updated 2026-02-26)

**Core value:** Open the app -> instantly see real-time departures for your commute routes
**Current focus:** Phase 1 - Backend STIB Integration

## Current Position

Phase: 1 of 2 (Backend STIB Integration)
Plan: 1 of 2 in current phase
Status: In progress
Last activity: 2026-03-02 — Completed plan 01-01 (API contract + STIB client)

Progress: [█░░░░░░░░░] 25%

## Performance Metrics

**Velocity:**
- Total plans completed: 1
- Average duration: 4 min
- Total execution time: 4 min

**By Phase:**

| Phase | Plans | Total | Avg/Plan |
|-------|-------|-------|----------|
| 01 - Backend STIB Integration | 1 | 4 min | 4 min |

**Recent Trend:**
- Last 5 plans: 4 min
- Trend: baseline

*Updated after each plan completion*

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

### Pending Todos

None yet.

### Blockers/Concerns

- STIB API key must be available as environment variable before Phase 1 can be tested end-to-end

## Session Continuity

Last session: 2026-03-02
Stopped at: Completed 01-01-PLAN.md (API contract + STIB client)
Resume file: .planning/phases/01-backend-stib-integration/01-02-PLAN.md
