---
id: T01
parent: S02
milestone: M001
provides:
  - Real API service (apiDashboardService) using fetch
  - Frontend entity types matching backend DeparturesResponse
  - React Query 30s auto-refresh via useDashboard hook
key_files:
  - frontend/src/infrastructure/api/api-dashboard-service.ts
  - frontend/src/entities/dashboard.ts
  - frontend/src/presentation/hooks/useDashboard.ts
key_decisions:
  - Plain fetch in infrastructure adapter, not kubb-generated client
  - Hardcode userName=Florent and city=Brussels (auth is v2)
  - Remove mock data service entirely
patterns_established:
  - IDashboardService async port (Promise<DeparturesData>)
  - React Query queryKey ['departures'] with refetchInterval 30_000
  - LineDepartures shows "No upcoming departures" for empty routes
observability_surfaces:
  - none
duration: 3min
verification_result: passed
completed_at: 2026-03-02
blocker_discovered: false
---

# T01: Real API wiring + type remodel + 30s auto-refresh

**React Query dashboard fetching real STIB departures from GET /api/departures with 30s auto-refresh, replacing mock data with live RouteDepartures types**

## What Happened

Remodeled frontend types: Departure (minutesUntilArrival, destination), RouteDepartures (stopName, lineNumber, direction, departures[]), DeparturesData (lastUpdated, routes[]) — exact match to backend. Created apiDashboardService using plain fetch to GET /api/departures. Migrated useDashboard from synchronous mock call to React Query useQuery with refetchInterval: 30_000. Deleted mock-dashboard-service.ts. Wired apiDashboardService into ServiceProvider in App.tsx.

## Verification

| # | Command | Exit Code | Verdict | Duration |
|---|---------|-----------|---------|----------|
| 1 | `npx tsc -b --noEmit` | 0 | pass | ~3s |
| 2 | `npx vitest run` | 0 | pass (2 tests) | ~2s |

## Deviations

- Fixed TypeScript erasableSyntaxOnly error — changed GetDepartures constructor from parameter shorthand to explicit field declaration
- Removed unused isLoading/error destructuring from DashboardPage (loading states are T02)

## Known Issues

None.

## Files Created/Modified

- `frontend/src/infrastructure/api/api-dashboard-service.ts` — Real fetch adapter (14 lines)
- `frontend/src/entities/dashboard.ts` — Remodeled types matching backend schema
- `frontend/src/use-cases/dashboard/get-departures.ts` — GetDepartures use case (replaces GetDashboard)
- `frontend/src/use-cases/dashboard/get-departures.test.ts` — 2 async tests
- `frontend/src/use-cases/ports/dashboard-service.ts` — Async port: getDepartures(): Promise<DeparturesData>
- `frontend/src/presentation/hooks/useDashboard.ts` — React Query with 30s refetch
- `frontend/src/presentation/pages/DashboardPage.tsx` — Renders data.routes via FavoriteRouteCard
- `frontend/src/presentation/components/FavoriteRouteCard.tsx` — Renders RouteDepartures[] list
- `frontend/src/presentation/components/LineDepartures.tsx` — "No upcoming departures" fallback
- `frontend/src/App.tsx` — Wires apiDashboardService into ServiceProvider
