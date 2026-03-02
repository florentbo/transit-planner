---
phase: 01-backend-stib-integration
plan: "01"
subsystem: backend-api-contract
tags: [openapi, stib, jackson, java-records, http-client]
dependency_graph:
  requires: []
  provides: [departures-api-contract, stib-http-client, stib-json-model]
  affects: [frontend-api-generation, stib-service-layer]
tech_stack:
  added:
    - java.net.http.HttpClient (JDK built-in HTTP client)
    - com.fasterxml.jackson.datatype.jsr310.JavaTimeModule (OffsetDateTime deserialization)
    - Custom Jackson deserializer for nested JSON string
  patterns:
    - Java records for immutable API response models
    - Custom StdDeserializer for passingtimes string-within-JSON
    - Spring @Value injection for external config (fail-fast on missing env var)
key_files:
  created:
    - api-spec/openapi.yaml (added /api/departures path + DeparturesResponse, RouteDepartures, Departure schemas)
    - backend/src/main/java/com/transit/client/StibApiClient.java
    - backend/src/main/java/com/transit/client/StibWaitingTimesResponse.java
    - backend/src/main/java/com/transit/client/StibApiException.java
    - backend/src/test/java/com/transit/client/StibWaitingTimesResponseTest.java
  modified:
    - backend/src/main/resources/application.yml (added stib.api.base-url and stib.api.key)
decisions:
  - "Used java.net.http.HttpClient with 5-second timeout (both connect and request) per user decision"
  - "StibApiException extends RuntimeException to propagate HTTP errors and timeouts"
  - "PassingTimesDeserializer uses its own ObjectMapper instance to avoid shared state issues"
  - "ADJUST_DATES_TO_CONTEXT_TIME_ZONE set to false to preserve +01:00 offset from STIB data"
  - "StibApiException created as dedicated exception class (Rule 2 - missing critical class)"
metrics:
  duration: "4 minutes"
  completed_date: "2026-03-02"
  tasks_completed: 2
  tasks_total: 2
  files_created: 5
  files_modified: 1
---

# Phase 01 Plan 01: API Contract + STIB Client Summary

**One-liner:** OpenAPI departures contract with DeparturesResponse schema driving codegen, plus STIB HTTP client with custom Jackson deserializer for the passingtimes-as-JSON-string pattern.

## Tasks Completed

| Task | Name | Commit | Files |
|------|------|--------|-------|
| 1 | Define departures endpoint in OpenAPI spec | 7ad5e24 | api-spec/openapi.yaml |
| 2 | Create STIB API client and response model with unit test | 8fc0de4 | StibApiClient.java, StibWaitingTimesResponse.java, StibApiException.java, StibWaitingTimesResponseTest.java, application.yml |

## What Was Built

### API Contract (Task 1)

Added `GET /api/departures` to `api-spec/openapi.yaml` with `security: []` override (no auth for v1). The response schema hierarchy is:

- `DeparturesResponse` — top-level with `lastUpdated` timestamp and `routes` array
- `RouteDepartures` — groups departures by stop/line/direction (`stopName`, `lineNumber`, `direction`, `departures`)
- `Departure` — individual departure with `minutesUntilArrival` and `destination`

The Gradle OpenAPI Generator task auto-generated `DeparturesApi.java` interface in `build/generated/` and the corresponding model classes (`DeparturesResponse`, `RouteDepartures`, `Departure`).

### STIB HTTP Client (Task 2)

`StibApiClient` uses `java.net.http.HttpClient` with 5-second timeouts to fetch from the STIB OpenDataSoft API. It URL-encodes the `where` clause for multiple point IDs (`pointid=8784 or pointid=5008`).

`StibWaitingTimesResponse` uses Java records for the response structure. The key challenge: STIB's `passingtimes` field is a JSON-encoded string (not a nested object). A custom `PassingTimesDeserializer` (extending `StdDeserializer`) reads the string value then re-parses it as `List<PassingTime>` using an inner `ObjectMapper` with `JavaTimeModule` registered.

`StibApiException` is a `RuntimeException` that carries HTTP errors and timeouts.

Three unit tests in `StibWaitingTimesResponseTest` verify: single result deserialization, multiple results, and timezone offset preservation (+01:00 must not be converted to UTC).

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 2 - Missing Critical] Created StibApiException class**

- **Found during:** Task 2
- **Issue:** `StibApiClient` needed a custom exception class (`StibApiException`) referenced in the implementation but not listed as a file in the plan.
- **Fix:** Created `StibApiException.java` extending `RuntimeException` with message and cause constructors.
- **Files modified:** `backend/src/main/java/com/transit/client/StibApiException.java` (new file)
- **Commit:** 8fc0de4

None other — plan executed as written.

## Verification Results

1. `./gradlew compileJava` — BUILD SUCCESSFUL (OpenAPI codegen + compilation)
2. `./gradlew test` — BUILD SUCCESSFUL (3 unit tests pass)
3. `api-spec/openapi.yaml` contains `GET /api/departures` with all required schemas
4. `DeparturesApi.java` generated at `build/generated/src/main/java/com/transit/api/`
5. `application.yml` has `stib.api.base-url` and `stib.api.key: ${STIB_API_KEY}`

## Self-Check: PASSED

All created files verified on disk. Both task commits (7ad5e24, 8fc0de4) confirmed in git log.
