# Requirements

## Validated

### R001 — Real-time STIB departures on dashboard
- Class: primary-user-loop
- Status: validated
- Description: User sees real-time STIB departures for hardcoded Brussels routes on dashboard
- Why it matters: Core value proposition — the reason the app exists
- Source: user
- Primary owning slice: M001/S01
- Supporting slices: M001/S02
- Validation: validated
- Notes: Verified via live STIB API integration and browser testing (v1.0)

### R002 — Departure details include line, direction, minutes
- Class: primary-user-loop
- Status: validated
- Description: Each departure shows line number, direction, and minutes until arrival
- Why it matters: Without these details, departure data is useless
- Source: user
- Primary owning slice: M001/S01
- Supporting slices: none
- Validation: validated
- Notes: Backend DeparturesService provides lineNumber, direction, minutesUntilArrival per departure

### R003 — 30-second auto-refresh
- Class: primary-user-loop
- Status: validated
- Description: Departures auto-refresh every 30 seconds without user action
- Why it matters: Transit data goes stale quickly — manual refresh is unacceptable for a commute tool
- Source: user
- Primary owning slice: M001/S02
- Supporting slices: none
- Validation: validated
- Notes: React Query refetchInterval: 30_000 — silent background refresh without loading flicker

### R004 — Loading state while fetching
- Class: quality-attribute
- Status: validated
- Description: Dashboard shows loading state while fetching departures
- Why it matters: Prevents blank screen on initial load
- Source: user
- Primary owning slice: M001/S02
- Supporting slices: none
- Validation: validated
- Notes: SkeletonCard with animate-pulse, shown only on initial load (isLoading && !data)

### R005 — Error state when API unavailable
- Class: failure-visibility
- Status: validated
- Description: Dashboard shows error state if STIB API is unavailable
- Why it matters: Users need to know when data can't be fetched, with option to retry
- Source: user
- Primary owning slice: M001/S02
- Supporting slices: none
- Validation: validated
- Notes: ErrorCard with "Try again" retry button; stale data preserved with amber warning on refresh failures

### R006 — Dashboard layout with route cards
- Class: primary-user-loop
- Status: validated
- Description: Dashboard displays commute routes with transport lines and departure pills
- Why it matters: Visual layout must match the commute use case — quick glance at departure times
- Source: user
- Primary owning slice: M001/S02
- Supporting slices: none
- Validation: validated
- Notes: FavoriteRouteCard → LineDepartures → DeparturePill component hierarchy

### R007 — Cloud Run deployment
- Class: operability
- Status: validated
- Description: Backend deployed to Google Cloud Run with STIB_API_KEY as environment variable
- Why it matters: App must be accessible outside localhost for real use
- Source: user
- Primary owning slice: M002/S01
- Supporting slices: none
- Validation: validated
- Notes: Live at https://transit-planner-backend-621870148637.europe-west1.run.app

## Deferred

### R008 — TfL London departures
- Class: core-capability
- Status: deferred
- Description: User sees real-time TfL departures for London routes
- Why it matters: Multi-city support is a future goal
- Source: user
- Primary owning slice: none
- Supporting slices: none
- Validation: unmapped
- Notes: Brussels first — London later

### R009 — Route persistence (PostgreSQL)
- Class: continuity
- Status: deferred
- Description: Routes persist across server restarts
- Why it matters: Hardcoded routes work for personal use but don't scale
- Source: user
- Primary owning slice: none
- Supporting slices: none
- Validation: unmapped
- Notes: Currently hardcoded in DeparturesService

### R010 — Route CRUD
- Class: core-capability
- Status: deferred
- Description: User can create, edit, and delete routes
- Why it matters: Routes are hardcoded — no user control yet
- Source: user
- Primary owning slice: none
- Supporting slices: none
- Validation: unmapped
- Notes: Not needed while routes are hardcoded

### R011 — Stop/line search
- Class: core-capability
- Status: deferred
- Description: User can search for stops by name and select lines
- Why it matters: Needed when routes are user-created instead of hardcoded
- Source: user
- Primary owning slice: none
- Supporting slices: none
- Validation: unmapped
- Notes: Not needed while routes are hardcoded

### R012 — User authentication (Clerk)
- Class: integration
- Status: deferred
- Description: User can sign up and log in via Clerk
- Why it matters: Personal app for now — authentication enables multi-user later
- Source: user
- Primary owning slice: none
- Supporting slices: none
- Validation: unmapped
- Notes: Personal app, single user for now

### R015 — Transport options on routes
- Class: core-capability
- Status: deferred
- Description: User can add specific transit lines to a route (provider, mode, line, boarding/alighting stops, direction)
- Why it matters: Routes without transport options can't drive departure lookups — currently hardcoded in backend
- Source: user
- Primary owning slice: none
- Supporting slices: none
- Validation: unmapped
- Notes: Requires route persistence (R009) first. Maps to roadmap Feature 4.

### R016 — Favorite routes
- Class: quality-attribute
- Status: deferred
- Description: User can star routes to pin them to the top of the dashboard
- Why it matters: Quick access to most-used routes when the user has many saved routes
- Source: user
- Primary owning slice: none
- Supporting slices: none
- Validation: unmapped
- Notes: Maps to roadmap Feature 7 (Quick Actions). Only valuable after route CRUD exists.

### R017 — City toggle (Brussels / London)
- Class: core-capability
- Status: deferred
- Description: User can switch context between Brussels and London to see routes and departures for each city
- Why it matters: Multi-city is a long-term goal — architecture supports it but UI doesn't yet
- Source: user
- Primary owning slice: none
- Supporting slices: none
- Validation: unmapped
- Notes: Maps to roadmap Feature 8. Requires TfL adapter (R008).

## Out of Scope

### R013 — Full DDD domain model
- Class: constraint
- Status: out-of-scope
- Description: Full DDD with aggregates, value objects, domain services
- Why it matters: Prevents premature over-engineering — evolve when complexity demands it
- Source: inferred
- Primary owning slice: none
- Supporting slices: none
- Validation: n/a
- Notes: Keep it simple — clean separation is enough until complexity demands more

### R014 — Offline/PWA support
- Class: anti-feature
- Status: out-of-scope
- Description: App works offline or as installable PWA
- Why it matters: Real-time data requires network — offline mode is meaningless
- Source: inferred
- Primary owning slice: none
- Supporting slices: none
- Validation: n/a
- Notes: Network required for real-time departure data

## Traceability

| ID | Class | Status | Primary owner | Supporting | Proof |
|---|---|---|---|---|---|
| R001 | primary-user-loop | validated | M001/S01 | M001/S02 | validated |
| R002 | primary-user-loop | validated | M001/S01 | none | validated |
| R003 | primary-user-loop | validated | M001/S02 | none | validated |
| R004 | quality-attribute | validated | M001/S02 | none | validated |
| R005 | failure-visibility | validated | M001/S02 | none | validated |
| R006 | primary-user-loop | validated | M001/S02 | none | validated |
| R007 | operability | validated | M002/S01 | none | validated |
| R008 | core-capability | deferred | none | none | unmapped |
| R009 | continuity | deferred | none | none | unmapped |
| R010 | core-capability | deferred | none | none | unmapped |
| R011 | core-capability | deferred | none | none | unmapped |
| R012 | integration | deferred | none | none | unmapped |
| R013 | constraint | out-of-scope | none | none | n/a |
| R014 | anti-feature | out-of-scope | none | none | n/a |
| R015 | core-capability | deferred | none | none | unmapped |
| R016 | quality-attribute | deferred | none | none | unmapped |
| R017 | core-capability | deferred | none | none | unmapped |

## Coverage Summary

- Active requirements: 0
- Mapped to slices: 0
- Validated: 7
- Deferred: 8
- Out of scope: 2
- Unmapped active requirements: 0
