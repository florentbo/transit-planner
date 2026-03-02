# Transit Planner

## What This Is

A personal transit app that shows real-time STIB departure times for your daily Brussels commute. Open the app, instantly see when your next metro/tram leaves from Woest (line 51) and Pannenhuis (line 6), with data refreshing every 30 seconds.

## Core Value

Open the app → instantly see real-time departures for your commute routes.

## Requirements

### Validated

- ✓ Real-time STIB departures for hardcoded Brussels routes on dashboard — v1.0
- ✓ Each departure shows line number, direction, and minutes until arrival — v1.0
- ✓ Departures auto-refresh every 30 seconds — v1.0
- ✓ Loading state while fetching departures — v1.0
- ✓ Error state if STIB API is unavailable — v1.0
- ✓ Dashboard layout with route card, transport lines, and departure pills — v1.0

### Active

(None yet — define for next milestone)

### Out of Scope

- TfL London departures — later milestone, Brussels first
- Route persistence (PostgreSQL) — hardcoded routes sufficient for now
- Route CRUD (create/edit/delete) — not needed while routes are hardcoded
- Stop/line search — not needed while routes are hardcoded
- User authentication (Clerk) — personal app, single user for now
- Multi-city switching UI — Brussels only for now
- Full DDD domain model — keep it simple, evolve when complexity demands it
- Offline/PWA support — network required for real-time data

## Context

Shipped v1.0 with 1,186 LOC TypeScript + 526 LOC Java.
Tech stack: Java 25 + Spring Boot 4.0 (backend), React 19 + TypeScript + Vite + React Query (frontend).
Contract-first API via OpenAPI 3.0 with code generation on both sides.

**User's Brussels commute:**
- Woest (station 5008) → line 51 → direction Gare du Midi
- Pannenhuis (station 8784) → line 6 → direction Elisabeth

**Architecture:**
- Backend: controller/service/client pattern (Spring Boot)
- Frontend: clean architecture (entities, use-cases/ports, infrastructure/adapters, presentation)
- API contract: `api-spec/openapi.yaml` generates Java interfaces (backend) and TypeScript types (frontend)

## Constraints

- **API Key**: STIB API key must be stored as environment variable, not hardcoded
- **Tech stack**: Java 25 + Spring Boot 4.0 (backend), React 19 + TypeScript + Vite (frontend)
- **Contract-first**: OpenAPI spec is source of truth for API contract
- **Architecture**: Keep it simple — no premature DDD layers. Clean separation is enough until complexity demands more

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| Departures first, auth later | Core value is seeing departure times, not logging in | ✓ Good — shipped core value in v1.0 |
| Port integration, not architecture | Old code works but has god-config and hardcoded keys | ✓ Good — clean STIB client with proper DI |
| Hardcoded routes for v1 | Fastest path to real departures | ✓ Good — enabled fast delivery |
| New domain naming (Route/Line/Departure) | Matches frontend entities, clearer than old Journey/Trip | ✓ Good — consistent across stack |
| Skip full DDD for v1 | Overkill for 2 phases of work, evolve when needed | ✓ Good — kept codebase lean |
| Custom Jackson deserializer for STIB | STIB returns JSON string inside JSON | ✓ Good — handles real API quirks cleanly |
| React Query for auto-refresh | Built-in refetchInterval, caching, stale data handling | ✓ Good — minimal code for complex behavior |
| Plain fetch over generated client | Infrastructure adapter owns HTTP concerns | ✓ Good — simple, no framework coupling |

---
*Last updated: 2026-03-02 after v1.0 milestone*
