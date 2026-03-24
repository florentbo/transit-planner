---
id: T01
parent: S01
milestone: M001
provides:
  - departures-api-contract (OpenAPI spec with DeparturesResponse schema)
  - stib-http-client (StibApiClient with 5s timeout)
  - stib-json-model (Java records with custom PassingTimesDeserializer)
key_files:
  - api-spec/openapi.yaml
  - backend/src/main/java/com/transit/client/StibApiClient.java
  - backend/src/main/java/com/transit/client/StibWaitingTimesResponse.java
  - backend/src/main/java/com/transit/client/StibApiException.java
key_decisions:
  - java.net.http.HttpClient with 5-second timeout (both connect and request)
  - PassingTimesDeserializer uses its own ObjectMapper instance to avoid shared state
  - ADJUST_DATES_TO_CONTEXT_TIME_ZONE set to false to preserve +01:00 offset from STIB data
  - StibApiException extends RuntimeException for HTTP error propagation
patterns_established:
  - Java records for immutable API response models
  - Custom StdDeserializer for JSON-encoded strings inside JSON
  - Spring @Value injection for external config (fail-fast on missing env var)
observability_surfaces:
  - none
duration: 4min
verification_result: passed
completed_at: 2026-03-02
blocker_discovered: false
---

# T01: OpenAPI contract + STIB API client

**OpenAPI departures contract with DeparturesResponse schema plus STIB HTTP client with custom Jackson deserializer for the passingtimes-as-JSON-string pattern**

## What Happened

Added GET /api/departures to openapi.yaml with security: [] override (no auth for v1). Response hierarchy: DeparturesResponse → RouteDepartures → Departure. OpenAPI Generator auto-generated DeparturesApi.java interface and model classes.

Created StibApiClient using java.net.http.HttpClient with 5-second timeouts, URL-encoding the where clause for multiple point IDs. StibWaitingTimesResponse uses Java records. The key challenge: STIB's passingtimes field is a JSON-encoded string, not a nested object. Custom PassingTimesDeserializer reads the string then re-parses it using an inner ObjectMapper with JavaTimeModule.

## Verification

| # | Command | Exit Code | Verdict | Duration |
|---|---------|-----------|---------|----------|
| 1 | `./gradlew compileJava` | 0 | pass | ~10s |
| 2 | `./gradlew test` | 0 | pass (3 tests) | ~5s |

## Deviations

Created StibApiException class not in original plan — needed for HTTP error and timeout propagation from StibApiClient.

## Known Issues

None.

## Files Created/Modified

- `api-spec/openapi.yaml` — Added /api/departures path + DeparturesResponse, RouteDepartures, Departure schemas
- `backend/src/main/java/com/transit/client/StibApiClient.java` — HTTP client with 5s timeout, URL-encoded where clause
- `backend/src/main/java/com/transit/client/StibWaitingTimesResponse.java` — Java records with PassingTimesDeserializer
- `backend/src/main/java/com/transit/client/StibApiException.java` — RuntimeException for STIB errors
- `backend/src/test/java/com/transit/client/StibWaitingTimesResponseTest.java` — 3 tests: single result, multiple results, timezone preservation
- `backend/src/main/resources/application.yml` — Added stib.api.base-url and stib.api.key
