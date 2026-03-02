# Phase 2: Frontend Live Integration - Context

**Gathered:** 2026-03-02
**Status:** Ready for planning

<domain>
## Phase Boundary

Replace mock dashboard data with real API calls to the backend departures endpoint. Add 30-second auto-refresh, loading states, and error handling. The dashboard layout (route cards, line badges, departure pills) stays the same structurally. No new features or capabilities — just wiring real data and handling the states that come with it.

</domain>

<decisions>
## Implementation Decisions

### Loading & refresh UX
- Skeleton cards for initial load — grey placeholder shapes matching the card layout so the user sees structure before data arrives
- Silent update during 30s auto-refresh — departure pills change values in place, no loading indicator or flicker
- Keep stale data + show warning if refresh fails — display last known departures but add a subtle warning so user knows data may be outdated
- Auto-refresh only, no manual refresh button — 30s is frequent enough for transit, keep UI clean

### Error state design
- Inline error in card area for first-load failure — friendly message like "Couldn't load departures" with a retry button. Header and greeting still display normally
- Retry button on error state — "Try again" button so user can act immediately, auto-refresh continues in background
- Friendly & brief error tone — casual, no technical jargon, one line
- Show partial results — if only some routes fail, display what works, show error state only on the failing card

### Data transition
- Backend drives the route data — dashboard calls GET /api/departures and renders whatever the backend returns (currently Woest line 51 + Pannenhuis line 6). No hardcoded routes in the frontend
- "No departures" message for empty lines — show the line info (number, direction) but replace departure pills with "No upcoming departures". Card stays visible
- Remove mock data service — delete mock-dashboard-service.ts, wire directly to real API. No dead code
- Adapt frontend types to API shape — remodel frontend types to match what the backend returns (departures grouped by stop). No translation layer

### Staleness indication
- No "last updated" timestamp — trust the 30s auto-refresh, keep UI minimal. Departure times themselves tell you it's live
- Staleness warning after threshold — after ~2 minutes without a successful refresh, show a subtle warning like "Data may be outdated" on the card area

### Claude's Discretion
- Exact skeleton card shimmer animation
- Staleness threshold timing (suggested ~2 minutes, can adjust)
- Error message exact wording
- How to structure the React Query polling setup
- Whether to use React Query's built-in refetch or a custom interval

</decisions>

<specifics>
## Specific Ideas

- Layout should match the existing mock: route cards with transport line badges and departure pills — this is already built, just needs real data piped in
- The existing clean architecture (entities, use-cases, infrastructure, presentation) should be maintained — swap the mock infrastructure implementation for a real API one

</specifics>

<deferred>
## Deferred Ideas

None — discussion stayed within phase scope

</deferred>

---

*Phase: 02-frontend-live-integration*
*Context gathered: 2026-03-02*
