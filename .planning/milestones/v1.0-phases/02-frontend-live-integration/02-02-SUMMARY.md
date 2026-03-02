---
phase: 02-frontend-live-integration
plan: "02"
subsystem: ui
tags: [react, typescript, tailwind, react-query, skeleton, error-handling]

# Dependency graph
requires:
  - phase: 02-frontend-live-integration
    provides: useDashboard hook returning React Query result with data/isLoading/error/dataUpdatedAt/refetch
provides:
  - SkeletonCard component — animated grey placeholder matching FavoriteRouteCard layout
  - ErrorCard component — friendly error message with "Try again" retry button
  - StalenessWarning component — subtle "Data may be outdated" warning after 2-minute threshold
  - DashboardPage updated with full loading/error/stale data state machine
affects: [any phase adding new route card components]

# Tech tracking
tech-stack:
  added: []
  patterns:
    - Early return / let-content pattern for rendering state branches in React
    - isLoading && !data for skeleton (not shown during background refresh)
    - error && !data for error card (preserve stale data when available)
    - Interval-based staleness check via useEffect with setInterval

key-files:
  created:
    - frontend/src/presentation/components/SkeletonCard.tsx
    - frontend/src/presentation/components/ErrorCard.tsx
    - frontend/src/presentation/components/StalenessWarning.tsx
  modified:
    - frontend/src/presentation/pages/DashboardPage.tsx

key-decisions:
  - "Skeleton only on initial load — isLoading && !data prevents flicker during background 30s refresh"
  - "Stale data preserved with amber warning when refresh fails — never blank the screen"
  - "StalenessWarning checks every 10s via interval, threshold 2 minutes"

patterns-established:
  - "State machine pattern: isLoading&&!data -> skeleton, error&&!data -> error card, data -> content"
  - "Background refresh failure: keep showing data + amber refresh-failure message"

requirements-completed: [DEPT-04, DEPT-05]

# Metrics
duration: 2min
completed: 2026-03-02
---

# Phase 02 Plan 02: Dashboard Loading States Summary

**Skeleton loading, error card with retry, and staleness warning added to departures dashboard using React Query state fields and Tailwind animate-pulse**

## Performance

- **Duration:** 2 min
- **Started:** 2026-03-02T22:26:07Z
- **Completed:** 2026-03-02T22:27:08Z
- **Tasks:** 2 (1 auto + 1 human-verify checkpoint, approved)
- **Files modified:** 4

## Accomplishments
- SkeletonCard with animated grey placeholders matching FavoriteRouteCard layout (aria-busy, aria-label accessible)
- ErrorCard with "Couldn't load departures right now." and "Try again" button
- StalenessWarning computing staleness from React Query's dataUpdatedAt, showing after 2-minute threshold
- DashboardPage state machine: initial skeleton -> error card (no data) -> data+stale warning

## Task Commits

Each task was committed atomically:

1. **Task 1: Skeleton loading, error card with retry, staleness warning** - `c553c7f` (feat)
2. **Task 2: Human verification checkpoint** - approved by user

**Plan metadata:** `a82d484` (docs: checkpoint — awaiting human verify)

## Files Created/Modified
- `frontend/src/presentation/components/SkeletonCard.tsx` — animated pulse skeleton matching card layout
- `frontend/src/presentation/components/ErrorCard.tsx` — friendly error with retry button
- `frontend/src/presentation/components/StalenessWarning.tsx` — 2-minute threshold staleness indicator
- `frontend/src/presentation/pages/DashboardPage.tsx` — wires all states using let-content pattern

## Decisions Made
- Skeleton only shown during initial load (`isLoading && !data`) — silent background refresh on subsequent polls
- Stale cached data preserved when refresh fails — shows amber "Having trouble refreshing" message
- StalenessWarning uses 10s polling interval inside useEffect to avoid re-rendering the full component tree

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered
None.

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- All loading/error/staleness UI states verified complete by user
- Frontend fully integrated with live STIB backend (real-time 30s auto-refresh confirmed)
- Skeleton, error, stale data, and fresh data states all visually confirmed
- Backend must be running with a valid STIB_API_KEY for end-to-end operation
- No blockers — auth (Clerk) and multi-city routes are v2 scope

---
*Phase: 02-frontend-live-integration*
*Completed: 2026-03-02*
