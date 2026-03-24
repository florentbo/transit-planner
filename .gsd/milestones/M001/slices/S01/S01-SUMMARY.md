---
id: S01
parent: M001
milestone: M001
provides:
  - GET /api/departures endpoint (OpenAPI contract-first)
  - STIB HTTP client with custom Jackson deserializer for nested JSON strings
  - DeparturesService with hardcoded Woest/Pannenhuis routes and direction filtering
  - DeparturesApiController with 502 error mapping
requires: []
affects:
  - S02
key_files:
  - api-spec/openapi.yaml
  - backend/src/main/java/com/transit/client/StibApiClient.java
  - backend/src/main/java/com/transit/client/StibWaitingTimesResponse.java
  - backend/src/main/java/com/transit/service/DeparturesService.java
  - backend/src/main/java/com/transit/controller/DeparturesApiController.java
key_decisions:
  - java.net.http.HttpClient with 5-second timeout (connect + request)
  - Custom PassingTimesDeserializer with its own ObjectMapper to avoid shared state
  - Clock injected as Spring @Bean for testable time-dependent logic
  - Private CommutRoute record for hardcoded route constants
  - Direction filtering uses equalsIgnoreCase on destination.fr()
patterns_established:
  - Clock Bean Pattern — Clock.systemDefaultZone() as @Bean, injected into services
  - Error propagation — StibApiException → ResponseStatusException(BAD_GATEWAY) → ErrorResponse
  - Custom Jackson deserializer for APIs returning JSON-encoded strings inside JSON
observability_surfaces:
  - none
drill_down_paths:
  - tasks/T01-SUMMARY.md
  - tasks/T02-SUMMARY.md
duration: 6min
verification_result: passed
completed_at: 2026-03-02
---

# S01: Backend STIB Integration

**OpenAPI departures contract with STIB HTTP client, DeparturesService, and REST controller — real-time Brussels departure data served via GET /api/departures**

## What Happened

T01 defined the API contract (GET /api/departures with DeparturesResponse/RouteDepartures/Departure schemas in openapi.yaml) and built the STIB HTTP client. The key challenge was STIB's passingtimes field being a JSON-encoded string inside JSON — solved with a custom StdDeserializer using its own ObjectMapper with JavaTimeModule. Three unit tests verify deserialization including timezone offset preservation.

T02 created DeparturesService with hardcoded Woest (5008/line 51) and Pannenhuis (8784/line 6) route definitions, direction filtering via equalsIgnoreCase, and minutesUntilArrival computed using injected Clock. DeparturesApiController implements the generated DeparturesApi interface with an ExceptionHandler mapping ResponseStatusException to structured ErrorResponse. Three unit tests cover happy path, API failure, and direction filtering.

## Verification

- `./gradlew compileJava` — OpenAPI codegen + compilation passes
- `./gradlew test` — 6 unit tests pass (3 deserialization + 3 service)
- Manual: `curl http://localhost:8080/api/departures` returns real STIB data

## Requirements Advanced

- R001 — Backend now serves real-time STIB departures via API
- R002 — Each departure includes lineNumber, direction, minutesUntilArrival

## Requirements Validated

- R001 — Validated via live STIB API integration (human verified)
- R002 — Validated via unit test assertions on response structure

## Deviations

- Created StibApiException class not listed in T01 plan — necessary for HTTP error propagation
- Added Clock @Bean to CorsConfig — needed for DeparturesService constructor injection

## Known Limitations

- Phase 1 has no VERIFICATION.md (created before verifier workflow existed)
- Routes are hardcoded as Java constants — no persistence or CRUD

## Files Created/Modified

- `api-spec/openapi.yaml` — Added /api/departures path with DeparturesResponse schema
- `backend/src/main/java/com/transit/client/StibApiClient.java` — STIB HTTP client with 5s timeout
- `backend/src/main/java/com/transit/client/StibWaitingTimesResponse.java` — Java records with custom deserializer
- `backend/src/main/java/com/transit/client/StibApiException.java` — Custom RuntimeException for STIB errors
- `backend/src/main/java/com/transit/service/DeparturesService.java` — Business logic with hardcoded routes
- `backend/src/main/java/com/transit/controller/DeparturesApiController.java` — REST controller with error handler
- `backend/src/test/java/com/transit/client/StibWaitingTimesResponseTest.java` — 3 deserialization tests
- `backend/src/test/java/com/transit/service/DeparturesServiceTest.java` — 3 service unit tests
- `backend/src/main/java/com/transit/config/CorsConfig.java` — Added Clock @Bean
- `backend/src/main/resources/application.yml` — Added stib.api.base-url and stib.api.key

## Forward Intelligence

### What the next slice should know
- GET /api/departures returns DeparturesResponse with lastUpdated timestamp and routes array
- Each RouteDepartures has stopName, lineNumber, direction, departures[] with minutesUntilArrival and destination
- STIB_API_KEY must be set as env var — app will fail at startup without it

### What's fragile
- Direction filtering relies on equalsIgnoreCase matching — if STIB changes casing, filter breaks silently

### Authoritative diagnostics
- `curl -s http://localhost:8080/api/departures | jq .` — fastest backend health check

### What assumptions changed
- STIB API docs didn't mention passingtimes is a JSON string inside JSON — discovered from real API response
