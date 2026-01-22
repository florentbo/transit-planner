# Personal Transit Planner - Architecture Document

## 1. Project Overview

A personal transit planning application allowing users to save and quickly access their frequent routes (home→work, home→pool, etc.) with multiple transport options (metro, bus, train).

### Tech Stack

| Layer | Technology |
|-------|------------|
| Frontend | React 18 + TypeScript |
| Backend | Java 21 + Spring Boot 3 |
| API Design | OpenAPI 3.0 (Contract-First) |
| Authentication | Clerk |
| Database | PostgreSQL |

### Supported Transit Providers

- **STIB/MIVB** — Brussels metro, tram, bus
- **SNCB/NMBS** — Belgian railways
- **TfL** — Transport for London

---

## 2. Frontend Architecture

**Clean Architecture (without DDD)**

Focus on separation of concerns and dependency inversion.

### Layer Structure

```
┌─────────────────────────────────────────────────────────┐
│                    PRESENTATION                         │
│         (Pages, Components, Hooks, Contexts)            │
└─────────────────────────┬───────────────────────────────┘
                          │ depends on
                          ▼
┌─────────────────────────────────────────────────────────┐
│                      USE CASES                          │
│              (Application Business Rules)               │
│    GetSavedRoutes, SelectTransportOption, etc.          │
└─────────────────────────┬───────────────────────────────┘
                          │ depends on
                          ▼
┌─────────────────────────────────────────────────────────┐
│                      ENTITIES                           │
│           (Plain TypeScript types/interfaces)           │
│         Route, TransportOption, Stop, User              │
└─────────────────────────────────────────────────────────┘
                          ▲
                          │ implements
┌─────────────────────────────────────────────────────────┐
│                  INFRASTRUCTURE                         │
│        (API clients, Clerk adapter, Storage)            │
└─────────────────────────────────────────────────────────┘
```

### Key Principles

- **Entities** are pure TypeScript, no framework dependencies
- **Use Cases** orchestrate business logic, depend only on abstractions (interfaces)
- **Infrastructure** implements interfaces defined by use cases (dependency inversion)
- **Presentation** consumes use cases via hooks/contexts

---

## 3. Backend Architecture

**DDD + Clean Architecture + Hexagonal (Ports & Adapters)**

### Layer Structure

```
┌─────────────────────────────────────────────────────────────────────┐
│                          ADAPTERS (Infrastructure)                  │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐ │
│  │   REST API  │  │ Persistence │  │   Auth      │  │  Real-Time  │ │
│  │  (Inbound)  │  │  (Outbound) │  │  (Outbound) │  │  Transit    │ │
│  │             │  │             │  │             │  │  (Outbound) │ │
│  │  OpenAPI    │  │  PostgreSQL │  │   Clerk     │  │ STIB, SNCB  │ │
│  │  generated  │  │  JPA/JDBC   │  │   JWT       │  │ TfL APIs    │ │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘ │
└─────────┼────────────────┼────────────────┼────────────────┼────────┘
          │                │                │                │
          │ implements     │ implements     │ implements     │ implements
          ▼                ▼                ▼                ▼
┌─────────────────────────────────────────────────────────────────────┐
│                             PORTS                                   │
│      (Interfaces defined by domain/application layer)               │
│                                                                     │
│   Inbound Ports              Outbound Ports                         │
│   ─────────────              ──────────────                         │
│   RouteService               RouteRepository                        │
│   UserService                UserRepository                         │
│   TransitService             AuthProvider                           │
│                              RealTimeTransitProvider                │
└─────────────────────────────────────────────────────────────────────┘
          ▲                              ▲
          │ uses                         │ uses
┌─────────────────────────────────────────────────────────────────────┐
│                      APPLICATION LAYER                              │
│                        (Use Cases)                                  │
│                                                                     │
│   CreateSavedRoute         GetNextDepartures                        │
│   GetUserRoutes            SelectTransportOption                    │
│   UpdateRoutePreference    GetRealTimeInfo                          │
└─────────────────────────────────────────────────────────────────────┘
          │
          │ orchestrates
          ▼
┌─────────────────────────────────────────────────────────────────────┐
│                        DOMAIN LAYER                                 │
│                   (DDD: Aggregates, Entities, VOs)                  │
│                                                                     │
│   Aggregates: User, SavedRoute                                      │
│   Entities: TransportOption, Stop, Line                             │
│   Value Objects: Location, Direction, TransitProvider               │
│   Domain Services: RouteMatcher                                     │
└─────────────────────────────────────────────────────────────────────┘
```

### Adapters Detail

| Adapter | Type | Purpose |
|---------|------|---------|
| **REST API** | Inbound | Exposes OpenAPI-generated endpoints |
| **Persistence** | Outbound | PostgreSQL via JPA, implements repositories |
| **Auth (Clerk)** | Outbound | JWT validation, user identity resolution |
| **STIB** | Outbound | Brussels real-time transit data |
| **SNCB** | Outbound | Belgian railways real-time data |
| **TfL** | Outbound | London transport real-time data |

> **Is Auth an Adapter?**  
> Yes. Authentication is an **outbound adapter**. The domain defines a port (e.g., `AuthProvider` interface) and the Clerk implementation is an adapter. This allows:
> - Domain to remain auth-provider agnostic
> - Easy testing with mock auth
> - Potential future switch to another provider

---

## 4. Domain Model

### Aggregates & Entities

```
┌─────────────────────────────────────────────────────────────────┐
│                         USER (Aggregate Root)                   │
├─────────────────────────────────────────────────────────────────┤
│  userId: UUID                                                   │
│  clerkExternalId: String                                        │
│  email: String                                                  │
│  preferredCity: City                                            │
│  createdAt: DateTime                                            │
└─────────────────────────────────────────────────────────────────┘
                              │
                              │ owns 0..*
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    SAVED ROUTE (Aggregate Root)                 │
├─────────────────────────────────────────────────────────────────┤
│  routeId: UUID                                                  │
│  userId: UUID                                                   │
│  name: String (e.g., "Home → Work")                             │
│  origin: Location                                               │
│  destination: Location                                          │
│  transportOptions: List<TransportOption>                        │
│  defaultOptionId: UUID (nullable)                               │
│  createdAt: DateTime                                            │
│  updatedAt: DateTime                                            │
└─────────────────────────────────────────────────────────────────┘
                              │
                              │ contains 1..*
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    TRANSPORT OPTION (Entity)                    │
├─────────────────────────────────────────────────────────────────┤
│  optionId: UUID                                                 │
│  provider: TransitProvider                                      │
│  mode: TransportMode                                            │
│  line: Line                                                     │
│  boardingStop: Stop                                             │
│  alightingStop: Stop                                            │
│  direction: Direction                                           │
│  typicalDuration: Duration                                      │
└─────────────────────────────────────────────────────────────────┘
```

### Value Objects

| Value Object | Attributes |
|--------------|------------|
| **Location** | name, latitude, longitude |
| **Stop** | stopId, name, provider |
| **Line** | lineId, name, provider, color |
| **Direction** | directionId, terminus |
| **TransitProvider** | STIB, SNCB, TFL (enum) |
| **TransportMode** | METRO, TRAM, BUS, TRAIN (enum) |
| **City** | BRUSSELS, LONDON (enum) |

---

## 5. API Definition (OpenAPI)

### Resources

| Resource | Description |
|----------|-------------|
| `/users/me` | Current authenticated user |
| `/routes` | User's saved routes |
| `/routes/{id}` | Single route CRUD |
| `/routes/{id}/options` | Transport options for a route |
| `/departures` | Real-time departures |

### Endpoints Overview

#### User

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/users/me` | Get current user profile |
| PUT | `/users/me` | Update user preferences |

#### Saved Routes

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/routes` | List user's saved routes |
| POST | `/routes` | Create a new saved route |
| GET | `/routes/{routeId}` | Get route details |
| PUT | `/routes/{routeId}` | Update a route |
| DELETE | `/routes/{routeId}` | Delete a route |

#### Transport Options

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/routes/{routeId}/options` | Add transport option to route |
| PUT | `/routes/{routeId}/options/{optionId}` | Update an option |
| DELETE | `/routes/{routeId}/options/{optionId}` | Remove an option |
| PUT | `/routes/{routeId}/default-option` | Set default option |

#### Real-Time Departures

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/departures` | Get next departures for a stop |
| | Query params: `stopId`, `provider`, `lineId`, `direction` |

### Request/Response Models

#### Route (Response)

```
Route:
  routeId: UUID
  name: String
  origin: Location
  destination: Location
  transportOptions: TransportOption[]
  defaultOptionId: UUID | null
  createdAt: DateTime
  updatedAt: DateTime
```

#### CreateRouteRequest

```
CreateRouteRequest:
  name: String (required)
  origin: Location (required)
  destination: Location (required)
```

#### TransportOption (Response)

```
TransportOption:
  optionId: UUID
  provider: "STIB" | "SNCB" | "TFL"
  mode: "METRO" | "TRAM" | "BUS" | "TRAIN"
  line:
    lineId: String
    name: String
    color: String | null
  boardingStop:
    stopId: String
    name: String
  alightingStop:
    stopId: String
    name: String
  direction:
    directionId: String
    terminus: String
  typicalDuration: Integer (minutes)
```

#### CreateTransportOptionRequest

```
CreateTransportOptionRequest:
  provider: "STIB" | "SNCB" | "TFL" (required)
  mode: "METRO" | "TRAM" | "BUS" | "TRAIN" (required)
  lineId: String (required)
  boardingStopId: String (required)
  alightingStopId: String (required)
  directionId: String (required)
  typicalDuration: Integer (optional)
```

#### DepartureInfo (Response)

```
DepartureInfo:
  stopId: String
  stopName: String
  line:
    lineId: String
    name: String
  direction: String
  departures:
    - expectedTime: DateTime
      waitTimeMinutes: Integer
      isRealTime: Boolean
      message: String | null
```

#### Location

```
Location:
  name: String
  latitude: Decimal
  longitude: Decimal
```

### Authentication

All endpoints (except health check) require:
- Header: `Authorization: Bearer <clerk_jwt_token>`
- Clerk JWT is validated by the Auth adapter
- User is resolved from `sub` claim

### Error Responses

```
ErrorResponse:
  code: String (e.g., "ROUTE_NOT_FOUND")
  message: String
  details: Object | null
  timestamp: DateTime
```

| HTTP Status | Code | Description |
|-------------|------|-------------|
| 400 | VALIDATION_ERROR | Invalid request body |
| 401 | UNAUTHORIZED | Missing or invalid token |
| 403 | FORBIDDEN | Not owner of resource |
| 404 | ROUTE_NOT_FOUND | Route does not exist |
| 404 | STOP_NOT_FOUND | Stop not found in provider |
| 502 | TRANSIT_API_ERROR | Real-time API unavailable |

---

## 6. External Transit APIs

### STIB/MIVB (Brussels)

- **Base URL**: `https://opendata-api.stib-mivb.be`
- **Auth**: API Key
- **Used for**: Real-time metro, tram, bus departures
- **Key endpoints**: Waiting times, stop info, line details

### SNCB/NMBS (Belgian Rail)

- **Base URL**: `https://api.irail.be` (iRail open API)
- **Auth**: None (public)
- **Used for**: Train departures, liveboard
- **Key endpoints**: Liveboard, connections

### TfL (Transport for London)

- **Base URL**: `https://api.tfl.gov.uk`
- **Auth**: App ID + App Key
- **Used for**: Tube, bus, rail departures
- **Key endpoints**: StopPoint arrivals, line status

---

## 7. Sequence Diagrams

### Get Next Departures for a Saved Route

```
┌──────┐     ┌─────────┐     ┌──────────────┐     ┌────────────┐     ┌──────────┐
│ User │     │ Frontend│     │ Backend API  │     │ Application│     │ Transit  │
│      │     │         │     │ (REST)       │     │ Layer      │     │ Adapter  │
└──┬───┘     └────┬────┘     └──────┬───────┘     └─────┬──────┘     └────┬─────┘
   │              │                 │                   │                 │
   │ Select route │                 │                   │                 │
   │──────────────>                 │                   │                 │
   │              │                 │                   │                 │
   │              │ GET /departures │                   │                 │
   │              │ ?stopId=X       │                   │                 │
   │              │ &lineId=Y       │                   │                 │
   │              │─────────────────>                   │                 │
   │              │                 │                   │                 │
   │              │                 │ GetNextDepartures │                 │
   │              │                 │───────────────────>                 │
   │              │                 │                   │                 │
   │              │                 │                   │ fetchDepartures │
   │              │                 │                   │─────────────────>
   │              │                 │                   │                 │
   │              │                 │                   │    STIB API     │
   │              │                 │                   │<─────────────────
   │              │                 │                   │                 │
   │              │                 │   DepartureInfo   │                 │
   │              │                 │<───────────────────                 │
   │              │                 │                   │                 │
   │              │ 200 OK + JSON   │                   │                 │
   │              │<─────────────────                   │                 │
   │              │                 │                   │                 │
   │ Show times   │                 │                   │                 │
   │<──────────────                 │                   │                 │
```

### Create a Saved Route with Transport Options

```
┌──────┐     ┌─────────┐     ┌─────────┐     ┌─────────────┐     ┌────────────┐
│ User │     │ Frontend│     │ REST API│     │ Application │     │ Repository │
└──┬───┘     └────┬────┘     └────┬────┘     └──────┬──────┘     └─────┬──────┘
   │              │               │                 │                  │
   │ Fill form    │               │                 │                  │
   │──────────────>               │                 │                  │
   │              │               │                 │                  │
   │              │ POST /routes  │                 │                  │
   │              │───────────────>                 │                  │
   │              │               │                 │                  │
   │              │               │ CreateSavedRoute│                  │
   │              │               │─────────────────>                  │
   │              │               │                 │                  │
   │              │               │                 │      save()      │
   │              │               │                 │──────────────────>
   │              │               │                 │                  │
   │              │               │                 │     Route        │
   │              │               │                 │<──────────────────
   │              │               │                 │                  │
   │              │ 201 Created   │                 │                  │
   │              │<───────────────                 │                  │
   │              │               │                 │                  │
   │              │ POST /routes/{id}/options       │                  │
   │              │───────────────>                 │                  │
   │              │               │ AddTransportOpt │                  │
   │              │               │─────────────────>                  │
   │              │               │                 │     update()     │
   │              │               │                 │──────────────────>
   │              │               │                 │                  │
   │              │ 201 Created   │                 │                  │
   │              │<───────────────                 │                  │
```

---

## 8. Project Structure Overview

### Frontend

```
Presentation Layer
    └── Pages, Components, Hooks, Contexts

Use Cases Layer
    └── Application business rules (e.g., GetSavedRoutes)

Entities Layer
    └── Pure TypeScript types

Infrastructure Layer
    └── API Client, Clerk Adapter, LocalStorage
```

### Backend

```
Adapters Layer
    ├── Inbound: REST Controllers (OpenAPI generated)
    └── Outbound: JPA Repositories, Clerk Client, STIB/SNCB/TfL Clients

Ports Layer
    └── Interfaces for inbound and outbound operations

Application Layer
    └── Use Cases orchestrating domain logic

Domain Layer
    └── Aggregates, Entities, Value Objects, Domain Services
```

---

## 9. Open Questions / Decisions to Make

| Question | Options | Decision |
|----------|---------|----------|
| Database per user or shared? | Shared with tenant ID | TBD |
| Cache real-time data? | Redis, in-memory, none | TBD |
| Multi-city support from day 1? | Yes / Later | TBD |
| Offline support? | PWA / None | TBD |
| Rate limiting strategy? | Per user, global | TBD |

---

## 10. Next Steps

1. **Finalize OpenAPI spec** in YAML format
2. **Set up repositories** (frontend + backend)
3. **Configure Clerk** project and get API keys
4. **Design database schema** from domain model
5. **Spike transit APIs** — test STIB, SNCB, TfL connectivity
