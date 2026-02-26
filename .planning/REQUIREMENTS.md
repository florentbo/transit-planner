# Requirements: Transit Planner

**Defined:** 2026-02-26
**Core Value:** Open the app -> instantly see real-time departures for your commute routes

## v1 Requirements

### Departures

- [ ] **DEPT-01**: User sees real-time STIB departures for hardcoded Brussels routes on dashboard
- [ ] **DEPT-02**: Each departure shows line number, direction, and minutes until arrival
- [ ] **DEPT-03**: Departures auto-refresh every 30 seconds
- [ ] **DEPT-04**: Dashboard shows loading state while fetching departures
- [ ] **DEPT-05**: Dashboard shows error state if STIB API is unavailable

### Dashboard

- [ ] **DASH-01**: Dashboard displays commute routes with transport lines and departure pills (matches current mock layout)

## v2 Requirements

### London Departures

- **LDEP-01**: User sees real-time TfL departures for London routes
- **LDEP-02**: City toggle to switch between Brussels and London

### Route Management

- **RMGT-01**: User can create new routes with origin/destination
- **RMGT-02**: User can edit existing routes
- **RMGT-03**: User can delete routes
- **RMGT-04**: Routes persist across server restarts (PostgreSQL)

### Search

- **SRCH-01**: User can search for stops by name
- **SRCH-02**: User can select lines available at a stop

### Authentication

- **AUTH-01**: User can sign up and log in via Clerk
- **AUTH-02**: Routes are scoped to authenticated user

## Out of Scope

| Feature | Reason |
|---------|--------|
| Full DDD domain model | Overkill for v1 scope, evolve when complexity demands it |
| Multi-city UI switching | Brussels only for v1 |
| Route sharing | Personal app, single user |
| Offline/PWA support | Network required for real-time data |
| Mobile app | Web-first |
| SNCB train departures | Focus on STIB metro/tram first |

## Traceability

| Requirement | Phase | Status |
|-------------|-------|--------|
| DEPT-01 | Phase 1 - Backend STIB Integration | Pending |
| DEPT-02 | Phase 1 - Backend STIB Integration | Pending |
| DEPT-03 | Phase 2 - Frontend Live Integration | Pending |
| DEPT-04 | Phase 2 - Frontend Live Integration | Pending |
| DEPT-05 | Phase 2 - Frontend Live Integration | Pending |
| DASH-01 | Phase 2 - Frontend Live Integration | Pending |

**Coverage:**
- v1 requirements: 6 total
- Mapped to phases: 6
- Unmapped: 0

---
*Requirements defined: 2026-02-26*
*Last updated: 2026-02-26 after roadmap creation*
