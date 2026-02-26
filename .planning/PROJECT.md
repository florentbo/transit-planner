# Transit Planner

## What This Is

A personal transit app that shows real-time departure times for your daily commute routes. Open the app, instantly see when your next metro/tram leaves. Built for Brussels (STIB/MIVB) first, with London (TfL) planned for later.

## Core Value

Open the app → instantly see real-time departures for your commute routes.

## Requirements

### Validated

(None yet — ship to validate)

### Active

- [ ] Real-time STIB departures for hardcoded Brussels routes on dashboard
- [ ] Each departure shows line number, direction, and minutes until arrival
- [ ] Departures auto-refresh every 30 seconds
- [ ] Loading state while fetching departures
- [ ] Error state if STIB API is unavailable
- [ ] Dashboard layout with route card, transport lines, and departure pills

### Out of Scope

- TfL London departures — later milestone, Brussels first
- Route persistence (PostgreSQL) — hardcoded routes sufficient for v1
- Route CRUD (create/edit/delete) — not needed while routes are hardcoded
- Stop/line search — not needed while routes are hardcoded
- User authentication (Clerk) — personal app, single user for now
- Multi-city switching UI — Brussels only for v1
- Full DDD domain model — keep it simple, evolve when complexity demands it

## Context

An older working backend exists at `~/personal-dev/public-transports/public-transports-back` with:
- Real STIB API integration (OpenDataSoft endpoint, API key available)
- Real TfL API integration
- Hardcoded journeys for Brussels (Woest station 5008/line 51, Pannenhuis station 8784/line 6) and London

The new project has a React 19 frontend with a mock dashboard already rendering departure data beautifully. The backend is a Spring Boot 4 skeleton with 2 basic route endpoints and in-memory storage.

**Strategy:** Port the STIB integration logic (HTTP calls, response mapping, minutes calculation) into the new backend. Don't port the old architecture or naming — use the new project's domain terms (Route → Lines → Departures).

**User's Brussels commute:**
- Woest (station 5008) → line 51 → direction Gare du Midi
- Pannenhuis (station 8784) → line 6 → direction Elisabeth

## Constraints

- **API Key**: STIB API key must be stored as environment variable, not hardcoded
- **Tech stack**: Java 25 + Spring Boot 4.0 (backend), React 19 + TypeScript + Vite (frontend)
- **Contract-first**: OpenAPI spec is source of truth for API contract
- **Architecture**: Keep it simple — no premature DDD layers. Clean separation (controller/service/client) is enough for v1

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| Departures first, auth later | Core value is seeing departure times, not logging in | — Pending |
| Port integration, not architecture | Old code works but has god-config and hardcoded keys | — Pending |
| Hardcoded routes for v1 | Fastest path to real departures | — Pending |
| New domain naming (Route/Line/Departure) | Matches frontend entities, clearer than old Journey/Trip | — Pending |
| Skip full DDD for v1 | Overkill for 3 phases of work, evolve when needed | — Pending |

---
*Last updated: 2026-02-26 after initialization*
