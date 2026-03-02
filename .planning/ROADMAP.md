# Roadmap: Transit Planner

## Overview

Two phases deliver the core value: first, wire the backend to real STIB departure data; second, connect the frontend to that real data with auto-refresh and proper loading/error states. The app then fulfills its promise — open it and instantly see when your next metro or tram leaves.

## Phases

- [x] **Phase 1: Backend STIB Integration** - Expose a real departures endpoint backed by the STIB OpenDataSoft API (completed 2026-03-02)
- [x] **Phase 2: Frontend Live Integration** - Replace mock data with real API calls, auto-refresh every 30s, and handle loading/error states (completed 2026-03-02)

## Phase Details

### Phase 1: Backend STIB Integration
**Goal**: The backend serves real departure times for hardcoded Brussels routes via a contract-first API endpoint
**Depends on**: Nothing (first phase)
**Requirements**: DEPT-01, DEPT-02
**Success Criteria** (what must be TRUE):
  1. Calling `GET /api/departures` returns real-time departures for Woest (line 51) and Pannenhuis (line 6)
  2. Each departure in the response includes line number, direction, and minutes until arrival
  3. The STIB API key is read from an environment variable, never hardcoded
  4. The endpoint is defined in `api-spec/openapi.yaml` and the backend implements the generated interface
**Plans**: 2 plans
- [x] 01-01-PLAN.md — OpenAPI contract + STIB API client with JSON parsing test
- [ ] 01-02-PLAN.md — DeparturesService + controller + end-to-end verification

### Phase 2: Frontend Live Integration
**Goal**: Users open the dashboard and see live departure times that stay current automatically
**Depends on**: Phase 1
**Requirements**: DEPT-03, DEPT-04, DEPT-05, DASH-01
**Success Criteria** (what must be TRUE):
  1. The dashboard displays real departure data from the backend (no hardcoded mock)
  2. Departures refresh automatically every 30 seconds without any user action
  3. A loading indicator is visible while the first fetch (or any refresh) is in flight
  4. When the STIB API is unavailable, the dashboard shows an error message instead of crashing
  5. The layout matches the existing mock: route cards, transport lines, and departure pills
**Plans**: 2 plans
- [ ] 02-01-PLAN.md — Real API wiring + type remodel + 30s auto-refresh
- [ ] 02-02-PLAN.md — Loading skeleton, error card, staleness warning + visual verification

## Progress

**Execution Order:**
Phases execute in numeric order: 1 → 2

| Phase | Plans Complete | Status | Completed |
|-------|----------------|--------|-----------|
| 1. Backend STIB Integration | 2/2 | Complete   | 2026-03-02 |
| 2. Frontend Live Integration | 2/2 | Complete   | 2026-03-02 |
