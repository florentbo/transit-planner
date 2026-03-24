# T01: OpenAPI contract + STIB API client

**Slice:** S01 — **Milestone:** M001

## Description

Define the departures endpoint in OpenAPI spec and create STIB HTTP client with JSON parsing.

## Must-Haves

- [x] GET /api/departures defined in openapi.yaml with DeparturesResponse schema
- [x] StibApiClient fetches from STIB OpenDataSoft API with 5s timeout
- [x] Custom Jackson deserializer handles passingtimes JSON-string-inside-JSON
- [x] Unit tests verify deserialization including timezone offset preservation

## Files

- `api-spec/openapi.yaml`
- `backend/src/main/java/com/transit/client/StibApiClient.java`
- `backend/src/main/java/com/transit/client/StibWaitingTimesResponse.java`
- `backend/src/main/java/com/transit/client/StibApiException.java`
- `backend/src/test/java/com/transit/client/StibWaitingTimesResponseTest.java`
- `backend/src/main/resources/application.yml`
