# Milestones

## v1.0 MVP (Shipped: 2026-03-02)

**Phases completed:** 2 phases, 4 plans
**Timeline:** 40 days (2026-01-22 → 2026-03-02)
**LOC:** 1,186 TypeScript + 526 Java

**Key accomplishments:**
- Real-time STIB departure data via OpenDataSoft API with custom JSON deserializer for nested string-within-JSON format
- Contract-first API with OpenAPI spec generating both Java interfaces and TypeScript client
- Frontend wired to live backend with 30-second auto-refresh via React Query
- Loading skeleton, error card with retry, and staleness warning for robust UX
- Clean architecture across both layers — DI, ports/adapters, type-safe end-to-end

**Delivered:** Open the app and instantly see when your next metro/tram leaves from Woest (line 51) and Pannenhuis (line 6)

---

