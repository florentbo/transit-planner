---
phase: 02-frontend-live-integration
plan: "01"
subsystem: ui
tags: [react, typescript, react-query, fetch, clean-architecture]

# Dependency graph
requires:
  - phase: 01-backend-stib-integration
    provides: GET /api/departures endpoint returning DeparturesResponse JSON
provides:
  - Real API service implementation (apiDashboardService) using fetch to GET /api/departures
  - Frontend entity types matching backend DeparturesResponse shape (RouteDepartures, DeparturesData)
  - React Query useDashboard hook with 30s refetchInterval auto-refresh
  - DI wired to real API service (no more mock data)
affects: [02-02-frontend-loading-error-states]

# Tech tracking
tech-stack:
  added: []
  patterns:
    - Plain fetch in infrastructure adapter (no kubb-generated client for data fetching)
    - React Query useQuery with refetchInterval for auto-refresh
    - Hardcoded user/city values in presentation layer (auth is v2)

key-files:
  created:
    - frontend/src/infrastructure/api/api-dashboard-service.ts
    - frontend/src/use-cases/dashboard/get-departures.ts
    - frontend/src/use-cases/dashboard/get-departures.test.ts
  modified:
    - frontend/src/entities/dashboard.ts
    - frontend/src/use-cases/ports/dashboard-service.ts
    - frontend/src/presentation/hooks/useDashboard.ts
    - frontend/src/presentation/pages/DashboardPage.tsx
    - frontend/src/presentation/components/FavoriteRouteCard.tsx
    - frontend/src/presentation/components/LineDepartures.tsx
    - frontend/src/App.tsx
  deleted:
    - frontend/src/use-cases/dashboard/get-dashboard.ts
    - frontend/src/use-cases/dashboard/get-dashboard.test.ts
    - frontend/src/infrastructure/mock/mock-dashboard-service.ts

key-decisions:
  - "Use plain fetch in infrastructure adapter, not kubb-generated client — clean architecture keeps HTTP handling in adapter"
  - "Hardcode userName=Florent and city=Brussels in presentation layer — auth/user context is a v2 concern"
  - "Remove mock data service entirely — no dead code as per context decision"
  - "DashboardPage renders data only when available — loading/error states deferred to plan 02-02"

patterns-established:
  - "IDashboardService port is async (Promise<DeparturesData>) — infrastructure adapters own HTTP error handling"
  - "React Query queryKey: ['departures'] with refetchInterval: 30_000 for 30s auto-refresh"
  - "LineDepartures shows No upcoming departures text when route.departures is empty"

requirements-completed: [DASH-01, DEPT-03]

# Metrics
duration: 3min
completed: 2026-03-02
---

# Phase 02 Plan 01: Frontend Live Integration Summary

**React Query dashboard fetching real STIB departures from GET /api/departures with 30s auto-refresh, replacing mock data with live RouteDepartures types**

## Performance

- **Duration:** 3 min
- **Started:** 2026-03-02T22:21:00Z
- **Completed:** 2026-03-02T22:24:00Z
- **Tasks:** 2
- **Files modified:** 11

## Accomplishments
- Frontend entity types remodeled to match backend DeparturesResponse shape (RouteDepartures replacing TransportLine/FavoriteRoute)
- Real API service created using plain fetch calling GET /api/departures
- useDashboard hook migrated from synchronous mock call to React Query with 30s refetchInterval
- Mock data service deleted, DI wired to real API service in App.tsx
- Kubb regenerated with departures types (DeparturesResponse, RouteDepartures, GetDepartures hooks)

## Task Commits

Each task was committed atomically:

1. **Task 1: Regenerate API client and remodel frontend types** - `4e1a251` (feat)
2. **Task 2: Create real API service, wire DI, and add 30s auto-refresh** - `12b3b2f` (feat)

**Plan metadata:** (docs commit follows)

## Files Created/Modified
- `frontend/src/infrastructure/api/api-dashboard-service.ts` - Real IDashboardService fetching from GET /api/departures
- `frontend/src/entities/dashboard.ts` - Remodeled: Departure, RouteDepartures, DeparturesData (matches backend)
- `frontend/src/use-cases/ports/dashboard-service.ts` - Updated to async: getDepartures(): Promise<DeparturesData>
- `frontend/src/use-cases/dashboard/get-departures.ts` - GetDepartures use case (replaces GetDashboard)
- `frontend/src/use-cases/dashboard/get-departures.test.ts` - 2 async use case tests
- `frontend/src/presentation/hooks/useDashboard.ts` - React Query useQuery with refetchInterval: 30_000
- `frontend/src/presentation/pages/DashboardPage.tsx` - Renders data.routes via FavoriteRouteCard
- `frontend/src/presentation/components/FavoriteRouteCard.tsx` - Renders RouteDepartures[] list
- `frontend/src/presentation/components/LineDepartures.tsx` - Renders RouteDepartures with no-departure fallback
- `frontend/src/App.tsx` - Wires apiDashboardService into ServiceProvider

## Decisions Made
- Used plain fetch in infrastructure adapter rather than kubb-generated client — clean architecture principle, adapter owns HTTP
- Hardcoded "Florent" and "Brussels" in presentation layer — user/city context is auth (v2), not available from departures API
- Removed mock-dashboard-service entirely — no dead code allowed per context decision
- DashboardPage renders departures only when data is available — loading/error states are Plan 02-02 scope

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] Fixed TypeScript erasableSyntaxOnly error in GetDepartures constructor**
- **Found during:** Task 1 (TypeScript compilation check)
- **Issue:** `private readonly` parameter shorthand in constructor flagged by `erasableSyntaxOnly` tsconfig flag
- **Fix:** Changed to explicit field declaration with `this.dashboardService = dashboardService` assignment
- **Files modified:** frontend/src/use-cases/dashboard/get-departures.ts
- **Verification:** `npx tsc -b --noEmit` passes with no errors
- **Committed in:** 12b3b2f (Task 2 commit)

**2. [Rule 1 - Bug] Removed unused isLoading/error destructuring from DashboardPage**
- **Found during:** Task 2 (TypeScript compilation check)
- **Issue:** Plan template included `isLoading` and `error` destructuring but DashboardPage doesn't use them yet (loading states are Plan 02-02)
- **Fix:** Changed `const { data, isLoading, error } = useDashboard()` to `const { data } = useDashboard()`
- **Files modified:** frontend/src/presentation/pages/DashboardPage.tsx
- **Verification:** `npx tsc -b --noEmit` passes with no errors
- **Committed in:** 12b3b2f (Task 2 commit)

---

**Total deviations:** 2 auto-fixed (2 bugs — TypeScript compilation errors)
**Impact on plan:** Both fixes necessary for TypeScript to compile. No scope creep.

## Issues Encountered
None beyond the TypeScript errors noted above.

## User Setup Required
None - no external service configuration required.

## Self-Check: PASSED

All created/modified files confirmed present. Both task commits (4e1a251, 12b3b2f) verified in git history.

## Next Phase Readiness
- Dashboard now fetches live data from GET /api/departures with 30s auto-refresh
- Plan 02-02 can add loading spinner, error message, and staleness indicator on top of this foundation
- Backend must be running with STIB_API_KEY set for end-to-end testing

---
*Phase: 02-frontend-live-integration*
*Completed: 2026-03-02*
