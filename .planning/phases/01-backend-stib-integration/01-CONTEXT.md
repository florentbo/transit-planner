# Phase 1: Backend STIB Integration - Context

**Gathered:** 2026-02-26
**Status:** Ready for planning

<domain>
## Phase Boundary

The backend serves real-time STIB departure times for 2 hardcoded Brussels commute routes via a single contract-first API endpoint. Frontend displays the data. No route CRUD, no persistence, no auth.

</domain>

<decisions>
## Implementation Decisions

### Response shape
- Include a "last updated" timestamp in the response so the frontend can show data freshness
- Return all available departures per line (don't cap — STIB typically provides 2-4)
- Include human-readable stop names in the response (frontend doesn't map IDs)

### Error behavior
- Return HTTP 502 Bad Gateway with structured error body when STIB API is down or unreachable
- 5-second timeout on STIB API calls
- No retries — fail fast, frontend auto-refreshes every 30s anyway
- Fail at startup if STIB API key is missing or invalid (catch misconfiguration early)

### Refresh & caching
- No caching for v1 — every frontend request hits STIB directly (personal app, single user)
- Same endpoint for auto and manual refresh — no cache-busting mechanism needed

### Route structure
- Single endpoint (GET /api/departures) returns all hardcoded routes in one response
- Routes hardcoded in Java code (constants), not in application config
- Only return the commute direction: Woest → line 51 → Gare du Midi, Pannenhuis → line 6 → Elisabeth
- Don't return both directions for a line, only the one matching the commute

### Claude's Discretion
- Response grouping structure (flat list vs nested by route — pick what fits the frontend best)
- Which fields to include per departure beyond line, direction, minutes
- Error response body structure
- How to map STIB API data to the response model

</decisions>

<specifics>
## Specific Ideas

- Old backend at `~/personal-dev/public-transports/public-transports-back` has working STIB integration to port from
- User's commute: Woest (station 5008) line 51 direction Gare du Midi, Pannenhuis (station 8784) line 6 direction Elisabeth
- STIB uses OpenDataSoft endpoint with API key

</specifics>

<deferred>
## Deferred Ideas

None — discussion stayed within phase scope

</deferred>

---

*Phase: 01-backend-stib-integration*
*Context gathered: 2026-02-26*
