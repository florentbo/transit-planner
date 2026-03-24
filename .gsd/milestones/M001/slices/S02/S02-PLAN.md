# S02: Frontend Live Integration

**Goal:** Users open the dashboard and see live departure times that stay current automatically.
**Demo:** Dashboard shows real STIB departures with 30s auto-refresh, loading skeleton, error card with retry, and staleness warning.

## Must-Haves

- Dashboard displays real departure data from backend GET /api/departures (no mock data)
- Departures refresh automatically every 30 seconds without user action
- Loading skeleton visible while first fetch is in flight
- Error card with retry button when backend is down
- Staleness warning after ~2 minutes without successful refresh
- Layout matches existing mock: route cards, transport lines, departure pills

## Tasks

- [x] **T01: Real API wiring + type remodel + 30s auto-refresh** `est:15m`
  - Why: Replace mock data with real API calls and add auto-refresh
  - Files: `api-dashboard-service.ts`, `dashboard.ts`, `useDashboard.ts`, `DashboardPage.tsx`, `App.tsx`
  - Do: Remodel frontend types to match backend DeparturesResponse. Create plain fetch adapter. Wire React Query with refetchInterval: 30_000. Delete mock data service.
  - Verify: `npx tsc -b --noEmit` + `npx vitest run`
  - Done when: Dashboard fetches real data from GET /api/departures with 30s auto-refresh

- [x] **T02: Loading skeleton, error card, staleness warning** `est:10m`
  - Why: Handle all UX states — initial load, API failure, stale data
  - Files: `SkeletonCard.tsx`, `ErrorCard.tsx`, `StalenessWarning.tsx`, `DashboardPage.tsx`
  - Do: SkeletonCard with animate-pulse on isLoading && !data. ErrorCard with retry on error && !data. StalenessWarning with 2-minute threshold via useEffect interval. Wire into DashboardPage state machine.
  - Verify: `npx tsc -b --noEmit` + `npx vitest run` + human browser verification
  - Done when: All loading/error/staleness states verified visually in browser

## Files Likely Touched

- `frontend/src/infrastructure/api/api-dashboard-service.ts`
- `frontend/src/entities/dashboard.ts`
- `frontend/src/use-cases/ports/dashboard-service.ts`
- `frontend/src/use-cases/dashboard/get-departures.ts`
- `frontend/src/presentation/hooks/useDashboard.ts`
- `frontend/src/presentation/pages/DashboardPage.tsx`
- `frontend/src/presentation/components/SkeletonCard.tsx`
- `frontend/src/presentation/components/ErrorCard.tsx`
- `frontend/src/presentation/components/StalenessWarning.tsx`
- `frontend/src/App.tsx`
