---
phase: 01-backend-stib-integration
plan: "02"
subsystem: backend-service-layer
tags: [spring-boot, stib, departures, service-layer, unit-test, mockito]
dependency_graph:
  requires:
    - phase: 01-backend-stib-integration
      plan: "01"
      provides: [stib-http-client, departures-api-contract]
  provides: [departures-service-layer, departures-rest-endpoint]
  affects: [frontend-departures-display]
tech_stack:
  added:
    - java.time.Clock (injected for deterministic time in tests)
    - Mockito (via spring-boot-starter-test — used for StibApiClient mock)
  patterns:
    - Clock injection pattern for testable time-dependent logic
    - ExceptionHandler on controller to map ResponseStatusException to structured ErrorResponse
    - CommutRoute private record for hardcoded route constants
requirements-completed:
  - DEPT-01
  - DEPT-02
key_files:
  created:
    - backend/src/main/java/com/transit/service/DeparturesService.java
    - backend/src/main/java/com/transit/controller/DeparturesApiController.java
    - backend/src/test/java/com/transit/service/DeparturesServiceTest.java
  modified:
    - backend/src/main/java/com/transit/config/CorsConfig.java (added Clock bean)
decisions:
  - "Clock injected as Spring bean (not created inline) to allow fixed Clock in unit tests"
  - "ExceptionHandler on DeparturesApiController catches ResponseStatusException and maps to structured ErrorResponse"
  - "Private CommutRoute record holds hardcoded route constants (pointId, stopName, lineId, direction) as a named type"
  - "Direction filter uses equalsIgnoreCase on destination.fr() against route direction string"
patterns-established:
  - "Clock Bean Pattern: Clock.systemDefaultZone() declared as @Bean in CorsConfig, injected into services for testability"
  - "Error propagation: DeparturesService wraps StibApiException in ResponseStatusException(BAD_GATEWAY), controller maps to ErrorResponse"
metrics:
  duration: "2 minutes"
  completed_date: "2026-03-02"
  tasks_completed: 1
  tasks_total: 2
  files_created: 3
  files_modified: 1
---

# Phase 01 Plan 02: DeparturesService + Controller Summary

**DeparturesService maps STIB waiting-times response to DeparturesResponse with hardcoded Woest/Pannenhuis routes, exposed via DeparturesApiController implementing generated DeparturesApi interface.**

## Performance

- **Duration:** 2 min
- **Started:** 2026-03-02T20:21:59Z
- **Completed:** 2026-03-02T20:23:xx (checkpoint — awaiting human end-to-end verification)
- **Tasks:** 1/2 (Task 2 is human verification checkpoint)
- **Files modified:** 4

## Accomplishments

- DeparturesService with hardcoded Woest (5008/line 51) and Pannenhuis (8784/line 6) route definitions
- Direction filtering: only departures matching the commute direction (case-insensitive fr() match)
- minutesUntilArrival computed as MINUTES.between(now, arrival) + 1 using injected Clock
- DeparturesApiController implements generated DeparturesApi interface, maps StibApiException to HTTP 502 + ErrorResponse
- 3 unit tests: happy path (2 routes, correct minutes), API failure (ResponseStatusException), direction filtering (excludes Roi Baudouin, keeps Elisabeth)
- Clock registered as Spring @Bean in CorsConfig for testability

## Task Commits

1. **Task 1: Create DeparturesService and DeparturesApiController with unit test** - `9e529a9` (feat)

## Files Created/Modified

- `backend/src/main/java/com/transit/service/DeparturesService.java` — Business logic: hardcoded routes, calls StibApiClient, maps to DeparturesResponse, direction filtering, Clock injection
- `backend/src/main/java/com/transit/controller/DeparturesApiController.java` — REST controller implementing DeparturesApi, ExceptionHandler for BAD_GATEWAY mapping
- `backend/src/test/java/com/transit/service/DeparturesServiceTest.java` — 3 unit tests with mocked StibApiClient and fixed Clock
- `backend/src/main/java/com/transit/config/CorsConfig.java` — Added Clock @Bean

## Decisions Made

- Clock injected as Spring bean (not created inline) to allow fixed Clock in unit tests — essential for deterministic minutesUntilArrival assertions
- ExceptionHandler on DeparturesApiController catches ResponseStatusException and maps to structured ErrorResponse (error="STIB_API_ERROR", message=reason)
- Private CommutRoute record holds hardcoded constants as a named type (more readable than parallel arrays)
- Direction filtering uses equalsIgnoreCase on destination.fr() — handles "ELISABETH" vs "Elisabeth" data variations

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 2 - Missing Critical] Added Clock @Bean to CorsConfig**

- **Found during:** Task 1
- **Issue:** DeparturesService constructor-injects `Clock`, but no Clock @Bean existed in the application context. Spring could not auto-wire it.
- **Fix:** Added `@Bean public Clock clock() { return Clock.systemDefaultZone(); }` to existing CorsConfig class.
- **Files modified:** `backend/src/main/java/com/transit/config/CorsConfig.java`
- **Verification:** `./gradlew test` passes (Spring context wires correctly)
- **Committed in:** 9e529a9 (Task 1 commit)

---

**Total deviations:** 1 auto-fixed (1 missing critical)
**Impact on plan:** Clock bean is a necessary infrastructure piece for the service to work. No scope creep.

## Issues Encountered

None.

## User Setup Required

Before Task 2 (end-to-end verification) can proceed:
- `STIB_API_KEY` environment variable must be set (from STIB OpenDataSoft portal)
- Start backend: `cd backend && STIB_API_KEY=your_key ./gradlew bootRun`
- Verify: `curl -s http://localhost:8080/api/departures | jq .`

## Next Phase Readiness

- Task 2 checkpoint: human must verify real STIB API integration end-to-end
- After checkpoint approval: Phase 1 fully complete, ready for Phase 2 (frontend integration)
- No blockers other than STIB_API_KEY availability

---

*Phase: 01-backend-stib-integration*
*Completed: 2026-03-02 (partial — checkpoint pending)*

## Self-Check: PASSED

Files verified on disk:
- `backend/src/main/java/com/transit/service/DeparturesService.java` — FOUND
- `backend/src/main/java/com/transit/controller/DeparturesApiController.java` — FOUND
- `backend/src/test/java/com/transit/service/DeparturesServiceTest.java` — FOUND
- `backend/src/main/java/com/transit/config/CorsConfig.java` — FOUND (modified)

Commit 9e529a9 confirmed in git log.
