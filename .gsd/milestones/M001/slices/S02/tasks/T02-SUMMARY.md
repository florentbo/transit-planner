---
id: T02
parent: S02
milestone: M001
provides:
  - SkeletonCard component (animated loading placeholder)
  - ErrorCard component (friendly error with retry)
  - StalenessWarning component (2-minute threshold indicator)
  - Full state machine in DashboardPage
key_files:
  - frontend/src/presentation/components/SkeletonCard.tsx
  - frontend/src/presentation/components/ErrorCard.tsx
  - frontend/src/presentation/components/StalenessWarning.tsx
  - frontend/src/presentation/pages/DashboardPage.tsx
key_decisions:
  - Skeleton only on initial load (isLoading && !data) — no flicker during background refresh
  - Stale data preserved with amber warning — never blank the screen
  - StalenessWarning uses 10s polling interval to avoid full component tree re-renders
patterns_established:
  - State machine pattern — isLoading&&!data → skeleton, error&&!data → error card, data → content
  - Background refresh failure — keep data visible + amber warning
observability_surfaces:
  - none
duration: 2min
verification_result: passed
completed_at: 2026-03-02
blocker_discovered: false
---

# T02: Loading skeleton, error card, staleness warning

**Skeleton loading, error card with retry, and staleness warning using React Query state fields and Tailwind animate-pulse**

## What Happened

Created SkeletonCard with animated grey placeholders matching FavoriteRouteCard layout (aria-busy, aria-label for accessibility). ErrorCard shows "Couldn't load departures right now." with "Try again" button calling refetch(). StalenessWarning computes staleness from React Query's dataUpdatedAt, checking every 10s via useEffect interval, showing "Data may be outdated" after 2 minutes.

DashboardPage wired with let-content pattern: header (DashboardHeader) and greeting (GreetingSection) always render; only the section content switches between skeleton/error/data states.

## Verification

| # | Command | Exit Code | Verdict | Duration |
|---|---------|-----------|---------|----------|
| 1 | `npx tsc -b --noEmit` | 0 | pass | ~3s |
| 2 | `npx vitest run` | 0 | pass (2 tests) | ~2s |
| 3 | `npx eslint .` | 0 | pass | ~2s |
| 4 | Human browser verification | — | pass (all 5 items) | — |

## Deviations

None — plan executed exactly as written.

## Known Issues

- StalenessWarning may briefly flash on mount when dataUpdatedAt is 0 (React Query default) — cosmetic edge case

## Files Created/Modified

- `frontend/src/presentation/components/SkeletonCard.tsx` — Animated pulse skeleton (25 lines)
- `frontend/src/presentation/components/ErrorCard.tsx` — Friendly error with retry button (15 lines)
- `frontend/src/presentation/components/StalenessWarning.tsx` — 2-minute threshold indicator (24 lines)
- `frontend/src/presentation/pages/DashboardPage.tsx` — Full state machine with let-content pattern
