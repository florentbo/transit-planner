# Queued Milestones

Remaining features from the product roadmap (`ROADMAP.md`), re-ordered based on v1.0 lessons. Departures shipped first — now build the route management foundation underneath it.

## Queue

### M003: Route Persistence & CRUD
**Why next:** Routes are hardcoded Java constants. Can't add new routes, can't remove old ones. This is the minimum to make the app useful beyond the author's exact commute.
**Scope:** PostgreSQL database, JPA entities, Flyway migrations, GET/PUT/DELETE /api/routes/{id} endpoints, route detail page, edit/delete UI.
**Maps to:** Original roadmap Features 2 (Persistent Routes) + 3 (Route Management) — combined because persistence without CRUD is useless and CRUD without persistence is impossible.
**Requirements:** R009 (persistence), R010 (CRUD)

### M004: Transport Options & Stop Search
**Why next:** After routes persist, users need to attach specific transit lines (metro 1 from Merode to Schuman). Without search, users would need to know stop IDs — unusable.
**Scope:** TransportOption entity + CRUD, stop autocomplete via STIB API, line selector, direction picker. Departures endpoint reads from saved transport options instead of hardcoded constants.
**Maps to:** Original roadmap Features 4 (Transport Options) + 5 (Stop & Line Search) — combined because transport options without search is unusable.
**Requirements:** R011 (stop/line search)

### M005: User Accounts
**Why next:** Once routes are user-created and persisted, authentication becomes necessary to scope data. Before this milestone, all routes are visible to everyone.
**Scope:** Clerk integration (JWT validation, user creation), User table, routes filtered by userId, protected frontend routes, sign up/login UI.
**Maps to:** Original roadmap Feature 1 (User Accounts)
**Requirements:** R012 (authentication)

### M006: Dashboard & Favorites
**Why next:** With auth + persisted routes + transport options, the dashboard can show personalized departure data. Favorite routes get pinned to the top for one-tap access.
**Scope:** Home dashboard redesign, favorite flag on routes, quick departure expand, city selector (prep for multi-city).
**Maps to:** Original roadmap Feature 7 (Quick Actions)

### M007: Multi-City (London)
**Why last:** Requires all infrastructure (auth, persistence, transport options) to be in place. TfL adapter is a new integration.
**Scope:** TfL API adapter, city field on routes, city toggle UI, London stop/line data.
**Maps to:** Original roadmap Feature 8 (Multi-City)
**Requirements:** R008 (TfL London departures)

## Notes

- Original roadmap suggested auth first (Feature 1). V1.0 proved departures-first was better — auth adds friction without value when there's nothing worth protecting. Auth moves to M005, after routes are worth persisting.
- Features 2+3 and 4+5 from the original roadmap are merged into single milestones because the pairs are inseparable in practice.
- Feature 6 (Live Departures) was shipped as M001 v1.0 — not in the queue.

---
*Queued: 2026-03-24*
*Source: ROADMAP.md (repo root)*
