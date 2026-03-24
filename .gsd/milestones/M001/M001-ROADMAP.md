# M001: v1.0 MVP

**Vision:** Open the app and instantly see when your next metro/tram leaves from your commute stops.

## Success Criteria

- User sees real-time STIB departures for Woest (line 51) and Pannenhuis (line 6) on the dashboard
- Departures auto-refresh every 30 seconds without user action
- Loading skeleton appears on initial load, error card with retry when STIB API is down
- Staleness warning appears after ~2 minutes without successful refresh

## Slices

- [x] **S01: Backend STIB Integration** `risk:high` `depends:[]`
  > After this: GET /api/departures returns real-time STIB departure data for Woest and Pannenhuis
- [x] **S02: Frontend Live Integration** `risk:medium` `depends:[S01]`
  > After this: Dashboard shows live departures with 30s auto-refresh, loading skeleton, error card, and staleness warning
