# Transit Planner

## What This Is

A personal transit app that shows real-time STIB departure times for your daily Brussels commute. Open the app, instantly see when your next metro/tram leaves from Woest (line 51) and Pannenhuis (line 6), with data refreshing every 30 seconds. Backend deployed to Google Cloud Run.

## Core Value

Open the app → instantly see real-time departures for your commute routes.

## Current State

Shipped v1.0 (2026-03-02) and deployed to Cloud Run (2026-03-03). 1,186 LOC TypeScript + 526 LOC Java.

**What works:**
- Real-time STIB departures via OpenDataSoft API with custom JSON deserializer
- Contract-first API (OpenAPI 3.0) generating code on both Java and TypeScript sides
- 30-second auto-refresh via React Query
- Loading skeleton, error card with retry, staleness warning after 2 minutes
- Backend live on Cloud Run

**What's hardcoded / missing:**
- Routes are Java constants in DeparturesService (Woest line 51, Pannenhuis line 6) — no persistence, no CRUD
- No authentication — personal single-user app
- Frontend API base URL hardcoded to localhost:8080
- No stop/line search — routes require knowing stop IDs
- Brussels only — no TfL London support yet

## Architecture / Key Patterns

- **Backend:** Java 25 + Spring Boot 4.0 — controller/service/client pattern
- **Frontend:** React 19 + TypeScript + Vite + React Query — clean architecture (entities, use-cases/ports, infrastructure/adapters, presentation)
- **API contract:** `api-spec/openapi.yaml` generates Java interfaces (backend) and TypeScript types + React Query hooks (frontend via Kubb)
- **Deployment:** Multi-stage Docker build, Google Cloud Run, STIB_API_KEY as env var

## Capability Contract

See `.gsd/REQUIREMENTS.md` for the explicit capability contract, requirement status, and coverage mapping.

## Milestone Sequence

- [x] M001: v1.0 MVP — Real-time STIB departures dashboard with auto-refresh, loading/error states
- [x] M002: Deploy to GCloud — Backend deployed to Cloud Run with STIB_API_KEY env var
- [ ] M003: Route Persistence & CRUD — User can create, edit, delete routes stored in PostgreSQL
- [ ] M004: Transport Options & Stop Search — Add transit lines to routes with stop/line autocomplete
- [ ] M005: User Accounts — Sign up/login via Clerk, routes scoped to authenticated user
- [ ] M006: Dashboard & Favorites — Home dashboard with favorite routes and one-tap departure access
- [ ] M007: Multi-City — TfL London support with city toggle
