# T02: Loading skeleton, error card, staleness warning

**Slice:** S02 — **Milestone:** M001

## Description

Add robust UX states for loading, error, and stale data scenarios.

## Must-Haves

- [x] SkeletonCard with animate-pulse matching FavoriteRouteCard layout
- [x] ErrorCard with "Couldn't load departures" message and "Try again" retry button
- [x] StalenessWarning with 2-minute threshold, 10s polling interval
- [x] DashboardPage state machine: skeleton → error → data (with staleness)
- [x] Header and greeting always render regardless of data state

## Files

- `frontend/src/presentation/components/SkeletonCard.tsx`
- `frontend/src/presentation/components/ErrorCard.tsx`
- `frontend/src/presentation/components/StalenessWarning.tsx`
- `frontend/src/presentation/pages/DashboardPage.tsx`
