---
phase: 02-frontend-live-integration
verified: 2026-03-02T23:33:00Z
status: human_needed
score: 7/7 must-haves verified
human_verification:
  - test: "Dashboard displays live departure times from STIB"
    expected: "Route cards appear with real stop names, line numbers, directions, and minute-until-arrival pills"
    why_human: "Requires running backend with STIB_API_KEY and opening browser — cannot verify live HTTP round-trip programmatically"
  - test: "Departures auto-refresh every 30 seconds without user action"
    expected: "Departure times update in place with no loading flicker or blank screen"
    why_human: "Requires observing the browser for >30 seconds to confirm React Query fires the next poll and pills update silently"
  - test: "Loading skeleton appears on initial page load"
    expected: "Grey animated placeholder shapes appear briefly before data arrives"
    why_human: "Requires hard-refreshing the browser and observing the transient loading state before data arrives"
  - test: "Error card with retry appears when backend is down"
    expected: "Stop the backend, hard-refresh — see 'Couldn't load departures right now.' with 'Try again' button; clicking it retries the fetch"
    why_human: "Requires stopping the backend process and observing browser behavior"
  - test: "Staleness warning appears after ~2 minutes without successful refresh"
    expected: "With data loaded and backend stopped, wait ~2 min — 'Data may be outdated' text appears below the card"
    why_human: "Requires waiting 2 minutes in a real browser session with backend down"
---

# Phase 02: Frontend Live Integration — Verification Report

**Phase Goal:** Users open the dashboard and see live departure times that stay current automatically
**Verified:** 2026-03-02T23:33:00Z
**Status:** human_needed
**Re-verification:** No — initial verification

## Goal Achievement

### Observable Truths

| # | Truth | Status | Evidence |
|---|-------|--------|----------|
| 1 | Dashboard displays real departure data from backend GET /api/departures (no mock data) | VERIFIED | `api-dashboard-service.ts` fetches `http://localhost:8080/api/departures` via plain fetch; `App.tsx` wires `apiDashboardService` into `ServiceProvider`; no mock references remain in codebase |
| 2 | Departures refresh automatically every 30 seconds without user action | VERIFIED | `useDashboard.ts` uses `useQuery` with `refetchInterval: 30_000`; TypeScript compiles; lint clean |
| 3 | Frontend types match the backend API response shape (RouteDepartures with stopName, lineNumber, direction) | VERIFIED | `entities/dashboard.ts` defines `Departure { minutesUntilArrival, destination }`, `RouteDepartures { stopName, lineNumber, direction, departures[] }`, `DeparturesData { lastUpdated, routes[] }` — exact match to backend schema |
| 4 | A loading skeleton is visible while the first fetch is in flight | VERIFIED | `SkeletonCard.tsx` exists with `animate-pulse` layout; `DashboardPage.tsx` renders it on `isLoading && !data` |
| 5 | When the backend is unavailable, the dashboard shows an error message with a retry button | VERIFIED | `ErrorCard.tsx` renders "Couldn't load departures right now." with "Try again" button; wired on `error && !data` in `DashboardPage.tsx` |
| 6 | After ~2 minutes without successful refresh, a staleness warning appears | VERIFIED | `StalenessWarning.tsx` uses `useEffect` + `setInterval` at 10s intervals, threshold `2 * 60 * 1000` ms; rendered with `dataUpdatedAt` from React Query |
| 7 | When the API fails with no cached data, an error card replaces the departures section while the header and greeting remain visible | VERIFIED | `DashboardPage.tsx` uses let-content pattern: header (`DashboardHeader`) and greeting (`GreetingSection`) always render; only the `<section>` content switches between skeleton/error/data |

**Score:** 7/7 truths verified

### Required Artifacts

| Artifact | Expected | Status | Details |
|----------|----------|--------|---------|
| `frontend/src/infrastructure/api/api-dashboard-service.ts` | Real API implementation using fetch to GET /api/departures | VERIFIED | 14 lines, implements `IDashboardService.getDepartures()`, throws on non-ok response, returns `response.json()` as `DeparturesData` |
| `frontend/src/entities/dashboard.ts` | Types matching backend DeparturesResponse shape | VERIFIED | 16 lines, defines `Departure`, `RouteDepartures`, `DeparturesData` — exact match to backend schema; old types (`TransportMode`, `TransportLine`, `FavoriteRoute`, `DashboardData`) removed |
| `frontend/src/presentation/hooks/useDashboard.ts` | React Query hook with 30s refetchInterval | VERIFIED | 13 lines, `useQuery<DeparturesData, Error>` with `queryKey: ['departures']`, `refetchInterval: 30_000` |
| `frontend/src/presentation/components/SkeletonCard.tsx` | Skeleton placeholder cards matching route card layout | VERIFIED | 25 lines, `animate-pulse`, `aria-busy="true"`, `aria-label="Loading departures"`, 2 skeleton rows |
| `frontend/src/presentation/components/ErrorCard.tsx` | Friendly error message with retry button | VERIFIED | 15 lines, "Couldn't load departures right now.", `<button onClick={onRetry}>Try again</button>` |
| `frontend/src/presentation/components/StalenessWarning.tsx` | Subtle warning when data may be outdated | VERIFIED | 24 lines, 2-minute threshold, 10s polling interval, renders `null` when fresh, shows "Data may be outdated" when stale |
| `frontend/src/use-cases/dashboard/get-departures.ts` | GetDepartures use case delegating to IDashboardService | VERIFIED | Async, delegates to `dashboardService.getDepartures()` |
| `frontend/src/use-cases/ports/dashboard-service.ts` | Async port: `getDepartures(): Promise<DeparturesData>` | VERIFIED | Correct async signature |
| `frontend/src/presentation/pages/DashboardPage.tsx` | Full state machine: loading / error / data | VERIFIED | Handles `isLoading&&!data`, `error&&!data`, `data` with stale message, all states |

### Key Link Verification

| From | To | Via | Status | Details |
|------|----|-----|--------|---------|
| `useDashboard.ts` | `http://localhost:8080/api/departures` | React Query `useQuery` calling `IDashboardService.getDepartures()` with `refetchInterval: 30_000` | WIRED | `queryFn: () => service.getDepartures()` confirmed; `refetchInterval: 30_000` literal present |
| `App.tsx` | `api-dashboard-service.ts` | `ServiceProvider` wiring real API service | WIRED | `import { apiDashboardService }` and `<ServiceProvider dashboardService={apiDashboardService}>` both present |
| `DashboardPage.tsx` | `SkeletonCard.tsx` | Rendered when `isLoading` is true and no data exists | WIRED | `if (isLoading && !data) { content = <SkeletonCard /> }` on line 17-19 |
| `DashboardPage.tsx` | `ErrorCard.tsx` | Rendered when error exists and no cached data | WIRED | `else if (error && !data) { content = <ErrorCard onRetry={() => refetch()} /> }` on line 20-22 |
| `DashboardPage.tsx` | `StalenessWarning.tsx` | Rendered with `dataUpdatedAt` from React Query when data is present | WIRED | `<StalenessWarning dataUpdatedAt={dataUpdatedAt} />` inside data branch |

### Requirements Coverage

| Requirement | Source Plan | Description | Status | Evidence |
|-------------|-------------|-------------|--------|----------|
| DEPT-03 | 02-01 | Departures auto-refresh every 30 seconds | SATISFIED | `useDashboard.ts` line 11: `refetchInterval: 30_000`; React Query polls silently without re-showing skeleton |
| DEPT-04 | 02-02 | Dashboard shows loading state while fetching departures | SATISFIED | `SkeletonCard.tsx` shown on `isLoading && !data` in `DashboardPage.tsx`; `aria-busy="true"` for accessibility |
| DEPT-05 | 02-02 | Dashboard shows error state if STIB API is unavailable | SATISFIED | `ErrorCard.tsx` shown on `error && !data` with retry button calling `refetch()`; stale-data preservation with amber warning when refresh fails |
| DASH-01 | 02-01 | Dashboard displays commute routes with transport lines and departure pills | SATISFIED | `FavoriteRouteCard.tsx` renders `RouteDepartures[]` list; `LineDepartures.tsx` renders line badge + direction + `DeparturePill` for each departure; "No upcoming departures" for empty lines |

**Orphaned requirements check:** REQUIREMENTS.md maps DEPT-03, DEPT-04, DEPT-05, DASH-01 to Phase 2. All four appear in plan frontmatter (DEPT-03 + DASH-01 in 02-01-PLAN.md; DEPT-04 + DEPT-05 in 02-02-PLAN.md). No orphaned requirements.

### Anti-Patterns Found

| File | Line | Pattern | Severity | Impact |
|------|------|---------|----------|--------|
| — | — | — | — | No anti-patterns found in any phase-modified file |

All modified files are substantive implementations. No TODO/FIXME/placeholder comments. No empty handlers. No static return stubs. Deleted files (`mock-dashboard-service.ts`, `get-dashboard.ts`, `get-dashboard.test.ts`) confirmed absent. No mock references remain in the codebase.

### Automated Checks

| Check | Result |
|-------|--------|
| `npx tsc -b --noEmit` | PASSED — zero errors |
| `npx vitest run` | PASSED — 2/2 tests (`get-departures.test.ts`) |
| `npx eslint .` | PASSED — zero warnings/errors |
| Commits documented in summaries | VERIFIED — `4e1a251`, `12b3b2f`, `c553c7f` all exist in git log |
| Mock service deleted | CONFIRMED — `frontend/src/infrastructure/mock/` directory does not exist |
| Old `get-dashboard` files deleted | CONFIRMED — neither `get-dashboard.ts` nor `get-dashboard.test.ts` exist |

### Human Verification Required

#### 1. Live Departure Data Displayed

**Test:** Start backend with `cd backend && STIB_API_KEY=<key> ./gradlew bootRun`, start frontend with `cd frontend && npm run dev`, open http://localhost:5173
**Expected:** Route cards appear with real STIB stop names (e.g. "Woest"), line numbers (e.g. "51"), directions, and minute-until-arrival pills showing real times
**Why human:** Requires running backend with real STIB API key and observing browser output — cannot verify a live HTTP round-trip programmatically

#### 2. Silent 30-Second Auto-Refresh

**Test:** With data loaded, wait 30+ seconds and observe the departure time pills
**Expected:** Departure time values change in place (e.g. "3 min" becomes "2 min") with no loading spinner, no blank state, no flicker
**Why human:** Requires observing timed browser behavior across a 30+ second window

#### 3. Loading Skeleton on Initial Load

**Test:** Hard-refresh the page (Ctrl+Shift+R) while backend is running
**Expected:** Grey animated placeholder shapes appear briefly (typically <1s) before departure cards with real data replace them
**Why human:** Transient loading state requires observing the page at the precise moment before the first fetch completes

#### 4. Error Card with Retry Button

**Test:** Stop the backend (Ctrl+C), then hard-refresh the frontend
**Expected:** "Couldn't load departures right now." message with "Try again" button appears. Clicking "Try again" triggers another fetch attempt. Starting the backend and clicking retry (or waiting for auto-refresh) restores the data view.
**Why human:** Requires stopping a process, refreshing browser, and interacting with UI elements

#### 5. Staleness Warning After 2 Minutes

**Test:** With data loaded, stop the backend and wait approximately 2 minutes
**Expected:** "Data may be outdated" text appears below the route card. The last-known departure data remains visible (no blank screen).
**Why human:** Requires a 2-minute wait in a real browser session with the backend stopped

### Gaps Summary

No gaps found. All seven observable truths are verified by the codebase. All artifacts exist and are substantive implementations. All key links are wired and confirmed. Requirements DEPT-03, DEPT-04, DEPT-05, and DASH-01 are all satisfied with evidence.

Five items require human verification to confirm end-to-end behavior in a live browser session with a real backend. These are inherently visual/temporal behaviors that cannot be verified by static code analysis.

---

_Verified: 2026-03-02T23:33:00Z_
_Verifier: Claude (gsd-verifier)_
