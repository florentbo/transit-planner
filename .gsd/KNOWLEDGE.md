# Knowledge

## K001 — STIB API returns JSON strings inside JSON
- Scope: integration
- Rule: STIB's `passingtimes` field is a JSON-encoded string, not a nested object. Requires custom deserializer.
- Source: M001/S01 execution — real API response differed from documentation
- When: 2026-03-02

## K002 — Capture real API responses as test fixtures
- Scope: testing
- Rule: Always capture real API responses early for WireMock/test fixtures — documentation lies about response shapes.
- Source: M001 retrospective — STIB's actual response had extra fields and nested JSON strings
- When: 2026-03-02

## K003 — Frontend types must match API shape from start
- Scope: architecture
- Rule: Keep frontend entity types aligned with the backend API response shape from day one. Diverging mock types from API types causes broad refactors later.
- Source: M001/S02 execution — type remodel touched many files because mock types diverged from API shape
- When: 2026-03-02

## K004 — React Query isLoading && !data for skeleton
- Scope: frontend
- Rule: Use `isLoading && !data` to show skeleton only on initial load, not during background refresh polls. Prevents loading flicker every 30 seconds.
- Source: M001/S02 — established pattern for auto-refresh UX
- When: 2026-03-02

## K005 — api-dashboard-service.ts hardcodes localhost:8080
- Scope: tech-debt
- Rule: The frontend API base URL is hardcoded to localhost:8080 in api-dashboard-service.ts instead of reading from config.ts. Needs config for deployment.
- Source: M001 audit — flagged as low-severity tech debt
- When: 2026-03-02
