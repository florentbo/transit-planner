---
id: T02
parent: S01
milestone: M001
provides:
  - departures-service-layer (DeparturesService with hardcoded routes)
  - departures-rest-endpoint (DeparturesApiController implementing generated interface)
key_files:
  - backend/src/main/java/com/transit/service/DeparturesService.java
  - backend/src/main/java/com/transit/controller/DeparturesApiController.java
  - backend/src/test/java/com/transit/service/DeparturesServiceTest.java
key_decisions:
  - Clock injected as Spring @Bean for testable time-dependent logic
  - ExceptionHandler maps ResponseStatusException to structured ErrorResponse
  - Private CommutRoute record for hardcoded route constants
  - Direction filtering uses equalsIgnoreCase on destination.fr()
patterns_established:
  - Clock Bean Pattern — Clock.systemDefaultZone() as @Bean, injected into services
  - Error propagation chain — StibApiException → ResponseStatusException(BAD_GATEWAY) → ErrorResponse
observability_surfaces:
  - none
duration: 2min
verification_result: passed
completed_at: 2026-03-02
blocker_discovered: false
---

# T02: DeparturesService + controller

**DeparturesService maps STIB response to DeparturesResponse with hardcoded Woest/Pannenhuis routes, exposed via controller implementing generated DeparturesApi interface**

## What Happened

Created DeparturesService with hardcoded CommutRoute records for Woest (5008/line 51/Gare du Midi) and Pannenhuis (8784/line 6/Elisabeth). Service calls StibApiClient, filters by direction using equalsIgnoreCase, and computes minutesUntilArrival as MINUTES.between(now, arrival) + 1 using injected Clock.

DeparturesApiController implements the generated DeparturesApi interface. ExceptionHandler catches ResponseStatusException and maps BAD_GATEWAY to structured ErrorResponse with STIB_API_ERROR code.

Added Clock @Bean to CorsConfig (only existing config class) for testability.

## Verification

| # | Command | Exit Code | Verdict | Duration |
|---|---------|-----------|---------|----------|
| 1 | `./gradlew test` | 0 | pass (6 total tests) | ~5s |

3 new unit tests: happy path (2 routes, correct minutes), API failure (ResponseStatusException), direction filtering (excludes wrong direction).

## Deviations

Added Clock @Bean to CorsConfig — not in plan but necessary for Spring to wire DeparturesService constructor.

## Known Issues

None.

## Files Created/Modified

- `backend/src/main/java/com/transit/service/DeparturesService.java` — Business logic with hardcoded routes, direction filtering, Clock injection
- `backend/src/main/java/com/transit/controller/DeparturesApiController.java` — REST controller with ExceptionHandler
- `backend/src/test/java/com/transit/service/DeparturesServiceTest.java` — 3 unit tests with mocked StibApiClient and fixed Clock
- `backend/src/main/java/com/transit/config/CorsConfig.java` — Added Clock @Bean
