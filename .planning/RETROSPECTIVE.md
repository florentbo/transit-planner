# Project Retrospective

*A living document updated after each milestone. Lessons feed forward into future planning.*

## Milestone: v1.0 — MVP

**Shipped:** 2026-03-02
**Phases:** 2 | **Plans:** 4

### What Was Built
- Real-time STIB departure API (backend) with custom JSON deserializer for STIB's quirky nested-string format
- Contract-first OpenAPI spec generating code on both Java and TypeScript sides
- Live dashboard with 30-second auto-refresh via React Query
- Robust UX states: skeleton loading, error card with retry, staleness warning after 2 minutes

### What Worked
- Contract-first approach kept backend and frontend in sync — no type mismatches at the boundary
- Capturing real STIB API responses for WireMock fixtures caught edge cases early (extra `message` field, nested JSON strings)
- Clean architecture on the frontend (entities/use-cases/infrastructure/presentation) made swapping mock for real service trivial
- React Query handled polling, caching, and stale data with minimal code

### What Was Inefficient
- Phase 1 was created before the verifier workflow existed — missing VERIFICATION.md required manual audit
- Frontend type remodel in Phase 2 touched many files because the mock types diverged from the API shape

### Patterns Established
- Honeycomb testing: WireMock once in service test, MockMvc + stubbed service for integration tests
- Custom Jackson deserializer pattern for APIs that return JSON-encoded strings inside JSON
- React Query `isLoading && !data` pattern to show skeleton only on initial load (not during 30s refresh)

### Key Lessons
1. Capture real API responses early — STIB's actual response shape was different from documentation
2. Contract-first pays off immediately in a 2-layer app — type safety across the wire with zero manual sync
3. Keep frontend types aligned with API shape from the start to avoid broad refactors later

### Cost Observations
- Model mix: orchestrator on opus, executors/verifiers on sonnet
- Notable: 2-phase milestone completed in a single session

---

## Cross-Milestone Trends

### Process Evolution

| Milestone | Phases | Plans | Key Change |
|-----------|--------|-------|------------|
| v1.0 | 2 | 4 | Initial delivery — established contract-first and honeycomb testing patterns |

### Cumulative Quality

| Milestone | Tests | Key Quality Win |
|-----------|-------|-----------------|
| v1.0 | Backend: WireMock + MockMvc; Frontend: Vitest | Zero type mismatches at API boundary |

### Top Lessons (Verified Across Milestones)

1. Capture real API responses as test fixtures — documentation lies
2. Contract-first with code generation prevents integration bugs
