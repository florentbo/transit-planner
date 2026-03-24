# M001: v1.0 MVP

**Gathered:** 2026-02-26
**Status:** Complete

## Project Description

A personal transit app showing real-time STIB departure times for a daily Brussels commute.

## Why This Milestone

Deliver the core value proposition: open the app and instantly see when the next metro/tram leaves from your commute stops.

## User-Visible Outcome

### When this milestone is complete, the user can:

- Open the dashboard and see real-time departure times for Woest (line 51) and Pannenhuis (line 6)
- Watch departures auto-refresh every 30 seconds without any user action
- See a loading skeleton on first load and an error card with retry if STIB API is unavailable

### Entry point / environment

- Entry point: http://localhost:5173 (frontend dev server)
- Environment: local dev (backend on 8080, frontend on 5173)
- Live dependencies involved: STIB OpenDataSoft API (requires STIB_API_KEY)

## Scope

### In Scope

- Backend STIB API client with custom JSON deserialization
- GET /api/departures endpoint with contract-first OpenAPI spec
- Frontend dashboard with real-time data, 30s auto-refresh, loading/error/staleness states
- Clean architecture on both frontend and backend

### Out of Scope / Non-Goals

- User authentication (Clerk) — personal app, single user
- Route persistence (PostgreSQL) — hardcoded routes sufficient
- Route CRUD — not needed while routes are hardcoded
- TfL London departures — Brussels first
- Full DDD domain model — keep it simple
