# S01: Backend STIB Integration

**Goal:** The backend serves real departure times for hardcoded Brussels routes via a contract-first API endpoint.
**Demo:** Calling GET /api/departures returns real-time departures for Woest (line 51) and Pannenhuis (line 6).

## Must-Haves

- GET /api/departures endpoint defined in OpenAPI spec and implemented
- Each departure includes line number, direction, and minutes until arrival
- STIB API key read from environment variable, never hardcoded
- HTTP 502 with structured error body when STIB API is down
- 5-second timeout on STIB API calls

## Tasks

- [x] **T01: OpenAPI contract + STIB API client with JSON parsing test** `est:15m`
  - Why: Define the API contract and build the HTTP client that talks to STIB
  - Files: `api-spec/openapi.yaml`, `StibApiClient.java`, `StibWaitingTimesResponse.java`, `StibApiException.java`
  - Do: Add GET /api/departures to OpenAPI spec with DeparturesResponse/RouteDepartures/Departure schemas. Create STIB HTTP client with custom Jackson deserializer for passingtimes JSON-string-inside-JSON.
  - Verify: `./gradlew compileJava` + `./gradlew test` (3 deserialization tests pass)
  - Done when: OpenAPI codegen generates DeparturesApi interface, STIB client parses real response format

- [x] **T02: DeparturesService + controller + end-to-end verification** `est:10m`
  - Why: Wire the STIB client to a service layer and expose via REST controller
  - Files: `DeparturesService.java`, `DeparturesApiController.java`, `DeparturesServiceTest.java`, `CorsConfig.java`
  - Do: Create DeparturesService with hardcoded Woest/Pannenhuis routes, direction filtering, Clock injection. Create controller implementing generated DeparturesApi interface with ExceptionHandler for 502.
  - Verify: `./gradlew test` (3 unit tests: happy path, API failure, direction filtering)
  - Done when: curl http://localhost:8080/api/departures returns real STIB data

## Files Likely Touched

- `api-spec/openapi.yaml`
- `backend/src/main/java/com/transit/client/StibApiClient.java`
- `backend/src/main/java/com/transit/client/StibWaitingTimesResponse.java`
- `backend/src/main/java/com/transit/client/StibApiException.java`
- `backend/src/main/java/com/transit/service/DeparturesService.java`
- `backend/src/main/java/com/transit/controller/DeparturesApiController.java`
- `backend/src/main/resources/application.yml`
