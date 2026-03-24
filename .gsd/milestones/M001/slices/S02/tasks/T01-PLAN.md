# T01: Real API wiring + type remodel + 30s auto-refresh

**Slice:** S02 — **Milestone:** M001

## Description

Replace mock data with real API calls and add 30-second auto-refresh.

## Must-Haves

- [x] Frontend types match backend DeparturesResponse shape
- [x] Real API adapter using plain fetch to GET /api/departures
- [x] React Query useDashboard hook with refetchInterval: 30_000
- [x] Mock data service deleted, DI wired to real API service
- [x] Dashboard renders RouteDepartures from live data

## Files

- `frontend/src/infrastructure/api/api-dashboard-service.ts`
- `frontend/src/entities/dashboard.ts`
- `frontend/src/use-cases/ports/dashboard-service.ts`
- `frontend/src/use-cases/dashboard/get-departures.ts`
- `frontend/src/use-cases/dashboard/get-departures.test.ts`
- `frontend/src/presentation/hooks/useDashboard.ts`
- `frontend/src/presentation/pages/DashboardPage.tsx`
- `frontend/src/presentation/components/FavoriteRouteCard.tsx`
- `frontend/src/presentation/components/LineDepartures.tsx`
- `frontend/src/App.tsx`
