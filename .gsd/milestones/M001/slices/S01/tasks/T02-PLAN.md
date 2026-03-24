# T02: DeparturesService + controller

**Slice:** S01 — **Milestone:** M001

## Description

Create the service layer and REST controller that wire the STIB client to the API endpoint.

## Must-Haves

- [x] DeparturesService with hardcoded Woest/Pannenhuis routes
- [x] Direction filtering — only departures matching commute direction
- [x] minutesUntilArrival computed using injected Clock
- [x] DeparturesApiController implements generated DeparturesApi interface
- [x] ExceptionHandler maps STIB failures to HTTP 502 + ErrorResponse
- [x] 3 unit tests: happy path, API failure, direction filtering

## Files

- `backend/src/main/java/com/transit/service/DeparturesService.java`
- `backend/src/main/java/com/transit/controller/DeparturesApiController.java`
- `backend/src/test/java/com/transit/service/DeparturesServiceTest.java`
- `backend/src/main/java/com/transit/config/CorsConfig.java`
