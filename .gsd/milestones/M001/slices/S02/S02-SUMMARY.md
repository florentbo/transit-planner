---
id: S02
parent: M001
milestone: M001
provides:
  - Real API service (apiDashboardService) using fetch to GET /api/departures
  - Frontend entity types matching backend DeparturesResponse shape
  - React Query useDashboard hook with 30s refetchInterval
  - SkeletonCard, ErrorCard, StalenessWarning UI components
  - Full loading/error/staleness state machine in DashboardPage
requires:
  - slice: S01
    provides: GET /api/departures endpoint returning DeparturesResponse JSON
affects: []
key_files:
  - frontend/src/infrastructure/api/api-dashboard-service.ts
  - frontend/src/entities/dashboard.ts
  - frontend/src/presentation/hooks/useDashboard.ts
  - frontend/src/presentation/pages/DashboardPage.tsx
  - frontend/src/presentation/components/SkeletonCard.tsx
  - frontend/src/presentation/components/ErrorCard.tsx
  - frontend/src/presentation/components/StalenessWarning.tsx
key_decisions:
  - Plain fetch in infrastructure adapter, not kubb-generated client
  - Hardcode userName=Florent and city=Brussels in presentation layer (auth is v2)
  - Remove mock data service entirely — no dead code
  - Skeleton only on initial load (isLoading && !data) — silent background refresh
  - Stale data preserved with amber warning when refresh fails
  - StalenessWarning checks every 10s via interval, threshold 2 minutes
patterns_established:
  - IDashboardService port is async (Promise<DeparturesData>)
  - React Query queryKey ['departures'] with refetchInterval 30_000
  - State machine pattern — isLoading&&!data → skeleton, error&&!data → error card, data → content
  - Background refresh failure — keep showing data + amber message
observability_surfaces:
  - none
drill_down_paths:
  - tasks/T01-SUMMARY.md
  - tasks/T02-SUMMARY.md
duration: 5min
verification_result: passed
completed_at: 2026-03-02
---

# S02: Frontend Live Integration

**React Query dashboard fetching real STIB departures with 30s auto-refresh, skeleton loading, error card with retry, and staleness warning**

## What Happened

T01 remodeled frontend types to match the backend DeparturesResponse shape (RouteDepartures replacing TransportLine/FavoriteRoute), created a plain fetch adapter calling GET /api/departures, migrated useDashboard to React Query with refetchInterval: 30_000, and deleted the mock data service. T02 layered on the UX states: SkeletonCard (animate-pulse, shown on initial load only), ErrorCard ("Couldn't load departures right now." with "Try again" button), and StalenessWarning (2-minute threshold, 10s polling interval). DashboardPage uses a let-content pattern for the state machine while header and greeting always render.

## Verification

- `npx tsc -b --noEmit` — zero errors
- `npx vitest run` — 2/2 tests pass (GetDepartures use case)
- `npx eslint .` — zero warnings/errors
- Phase 2 VERIFICATION.md: 7/7 observable truths verified, 5 human items (live browser)
- Commits: 4e1a251, 12b3b2f, c553c7f verified in git log

## Requirements Validated

- R003 — 30s auto-refresh confirmed via React Query refetchInterval
- R004 — Skeleton loading shown on isLoading && !data
- R005 — Error card with retry on error && !data, stale data preserved with warning
- R006 — FavoriteRouteCard → LineDepartures → DeparturePill layout matches spec

## Deviations

- Fixed TypeScript erasableSyntaxOnly error in GetDepartures constructor — changed to explicit field declaration
- Removed unused isLoading/error destructuring from DashboardPage (loading states added in T02)

## Known Limitations

- api-dashboard-service.ts hardcodes http://localhost:8080 — needs config for deployment
- StalenessWarning may briefly flash on mount when dataUpdatedAt is 0

## Files Created/Modified

- `frontend/src/infrastructure/api/api-dashboard-service.ts` — Real API adapter using fetch
- `frontend/src/entities/dashboard.ts` — Remodeled types (Departure, RouteDepartures, DeparturesData)
- `frontend/src/use-cases/dashboard/get-departures.ts` — GetDepartures use case (replaces GetDashboard)
- `frontend/src/use-cases/dashboard/get-departures.test.ts` — 2 async tests
- `frontend/src/use-cases/ports/dashboard-service.ts` — Async port interface
- `frontend/src/presentation/hooks/useDashboard.ts` — React Query with 30s refetch
- `frontend/src/presentation/pages/DashboardPage.tsx` — Full state machine
- `frontend/src/presentation/components/SkeletonCard.tsx` — Animated skeleton
- `frontend/src/presentation/components/ErrorCard.tsx` — Friendly error with retry
- `frontend/src/presentation/components/StalenessWarning.tsx` — 2-minute threshold warning
- `frontend/src/presentation/components/FavoriteRouteCard.tsx` — Updated for RouteDepartures
- `frontend/src/presentation/components/LineDepartures.tsx` — Updated with no-departure fallback
- `frontend/src/App.tsx` — Wires apiDashboardService into ServiceProvider

## Forward Intelligence

### What the next slice should know
- Frontend types in entities/dashboard.ts match the backend DeparturesResponse exactly
- DI uses React Context ServiceProvider — swap implementations by changing App.tsx wiring
- Mock data service is deleted — no fallback if backend is unavailable during development

### What's fragile
- Hardcoded localhost:8080 in api-dashboard-service.ts — immediate issue for any non-local deployment

### Authoritative diagnostics
- Browser devtools Network tab — watch /api/departures calls every 30s
- React Query devtools (not installed) would show query state

### What assumptions changed
- Frontend types had to be fully remodeled — mock types diverged too far from API shape
