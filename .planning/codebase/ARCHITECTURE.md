# Architecture

**Analysis Date:** 2026-02-26

## Pattern Overview

**Overall:** DDD (Domain-Driven Design) + Hexagonal Architecture (Backend) / Clean Architecture (Frontend)

**Key Characteristics:**
- Backend implements DDD + Hexagonal (ports & adapters) with layered dependency inversion
- Frontend implements clean architecture with entities, use cases, infrastructure, and presentation layers
- Contract-first API design via OpenAPI 3.0 (single source of truth)
- Separation of concerns across domain, application, and infrastructure layers
- Dependency direction: always inward (infrastructure → application → domain)

## Layers

**Backend Domain Layer:**
- Purpose: Pure business logic with zero framework dependencies
- Location: `backend/src/main/java/com/transit/domain/` (target structure per ARCHITECTURE-DECISIONS.md)
- Contains: Aggregates (User, SavedRoute), Entities (TransportOption), Value Objects (Location, Stop, Line, Direction), Domain Services
- Depends on: Nothing (pure Java)
- Used by: Application layer

**Backend Application Layer:**
- Purpose: Use cases and ports (interfaces) for business operations
- Location: `backend/src/main/java/com/transit/application/` (target structure)
- Contains: Use cases (CreateSavedRoute, GetUserRoutes, etc.), command/query DTOs, port interfaces (UserRepository, SavedRouteRepository, RealTimeTransitProvider, AuthProvider)
- Depends on: Domain layer
- Used by: Infrastructure layer

**Backend Infrastructure Layer:**
- Purpose: Framework-specific implementations (Spring, JPA, external APIs)
- Location: `backend/src/main/java/com/transit/` (current: controller/, service/, config/)
- Contains: REST controllers (implementing OpenAPI-generated interfaces), JPA repositories, transit provider adapters, Clerk auth adapter, Spring configuration
- Depends on: Application and domain layers
- Used by: Spring Boot runtime

**Frontend Entities Layer:**
- Purpose: Pure TypeScript business types and validation logic
- Location: `frontend/src/entities/`
- Contains: Type definitions (DashboardData, FavoriteRoute, TransportLine, Departure), validator functions
- Depends on: Nothing (pure TypeScript)
- Used by: Use cases, presentation

**Frontend Use Cases Layer:**
- Purpose: Application business logic orchestrating entities
- Location: `frontend/src/use-cases/`
- Contains: Use case classes (GetDashboard), command/query objects, port interfaces (IDashboardService)
- Depends on: Entities
- Used by: Infrastructure, presentation hooks

**Frontend Infrastructure Layer:**
- Purpose: Technical implementations (API clients, auth, DI)
- Location: `frontend/src/infrastructure/`
- Contains: API client wrapper (client.ts), service implementations, mock services for testing, React Context DI providers
- Depends on: Use cases, entities
- Used by: Presentation layer

**Frontend Presentation Layer:**
- Purpose: React UI components, pages, hooks
- Location: `frontend/src/presentation/`
- Contains: Pages (DashboardPage), components (DashboardHeader, FavoriteRouteCard, DeparturePill), custom hooks (useDashboard), utilities
- Depends on: Use cases via hooks, infrastructure via context
- Used by: App.tsx root component

## Data Flow

**Create Saved Route (MVP):**

1. User submits form in CreateRoutePage (presentation)
2. Component calls useCreateRoute hook (presentation hook)
3. Hook instantiates CreateRoute use case with injected repository (use case)
4. Use case calls RouteRepository.save() via port interface (application)
5. API Repository adapter calls generated OpenAPI client (infrastructure)
6. POST /api/routes sent to backend RoutesApiController
7. Controller delegates to RouteService (current implementation)
8. RouteService stores in ConcurrentHashMap (MVP in-memory storage)
9. Response flows back through same chain
10. Component receives SavedRouteResponse and updates local state

**Get Saved Routes (MVP):**

1. Dashboard page loads, calls useSavedRoutes hook (presentation)
2. Hook instantiates GetSavedRoutes use case (use case)
3. Use case calls RouteRepository.list() (application port)
4. API Repository adapter calls generated OpenAPI client (infrastructure)
5. GET /api/routes sent to backend RoutesApiController
6. Controller delegates to RouteService.getAllRoutes()
7. RouteService returns list from ConcurrentHashMap
8. Response flows back to presentation, hook manages state via React Query

**State Management:**
- Backend: In-memory ConcurrentHashMap (RouteService) — MVP only
- Frontend: React Query caching (via generated hooks from Kubb), local component state
- Cross-layer: No shared state — each layer maintains its own data representation

## Key Abstractions

**Backend Aggregates:**
- Purpose: Cluster of objects treated as single unit with consistency boundaries
- Examples: `User` (maps to clerk auth), `SavedRoute` (contains TransportOptions)
- Pattern: Aggregate root enforces invariants, contains entities, references other aggregates by ID only

**Backend Value Objects:**
- Purpose: Immutable objects defined by attributes, not identity
- Examples: `Location` (name, lat, lon), `Stop`, `Line`, `Direction`, `TransitProvider`, `TransportMode`, `City`
- Pattern: Embedded in aggregates or entities, compared by value equality, no setters

**Backend Ports (Interfaces):**
- Purpose: Define contracts that domain/application need from external systems
- Examples: `UserRepository`, `SavedRouteRepository`, `RealTimeTransitProvider`, `AuthProvider`
- Pattern: Defined in application/domain layers, implemented in infrastructure (dependency inversion)

**Frontend Entities (Types):**
- Purpose: Pure TypeScript representation of domain concepts
- Examples: `DashboardData`, `FavoriteRoute`, `TransportLine`, `Departure`
- Pattern: Interfaces/types with no methods, validator functions separate

**Frontend Use Cases:**
- Purpose: Orchestrate business operations
- Examples: `GetDashboard` (single use case demonstrating pattern)
- Pattern: Receive dependencies via constructor, return domain entities

**Frontend Port Interfaces:**
- Purpose: Depend on abstractions, not implementations
- Examples: `IDashboardService` defined in use-cases/ports/
- Pattern: Infrastructure implements, presentation consumes via context injection

## Entry Points

**Backend REST API:**
- Location: `backend/src/main/java/com/transit/controller/RoutesApiController.java`
- Triggers: HTTP requests (POST /api/routes, GET /api/routes)
- Responsibilities: Map HTTP requests to commands, call use cases via RouteService, map responses to API DTOs
- Current: Implements OpenAPI-generated RoutesApi interface

**Backend Spring Boot Application:**
- Location: `backend/src/main/java/com/transit/TransitApplication.java`
- Triggers: `./gradlew bootRun` or `java -jar`
- Responsibilities: Bootstrap Spring context, auto-configure components, expose HTTP server on port 8080

**Frontend React Root:**
- Location: `frontend/src/App.tsx`
- Triggers: `npm run dev` or production build
- Responsibilities: Wrap components with ServiceProvider (DI), render DashboardPage

**Frontend Main Entry:**
- Location: `frontend/src/main.tsx`
- Triggers: Browser loads index.html
- Responsibilities: Render App into #root DOM element

## Error Handling

**Strategy:** Domain exceptions caught at application layer, mapped to HTTP status codes at infrastructure

**Patterns:**
- Domain layer: Throws domain-specific exceptions (RouteNotFoundException, InvalidEmailException)
- Application layer: Catches exceptions, returns result objects or rethrows
- Infrastructure (REST): Maps exceptions to HTTP status (400 validation, 404 not found, 500 server error)
- Frontend: API client handles HTTP errors, use cases propagate as failed promises

**Current MVP:** Minimal error handling (no validation, no custom exceptions) — errors will be added in Feature 2

## Cross-Cutting Concerns

**Logging:** Not yet implemented (use Spring Actuator for backend, console for frontend MVP)

**Validation:** Not yet enforced — OpenAPI spec defines constraints, will add validators in Feature 2

**Authentication:** JWT from Clerk is defined in OpenAPI spec (`bearerAuth`) but not validated in MVP. AuthProvider port ready for implementation.

**CORS:** Configured at `backend/src/main/java/com/transit/config/CorsConfig.java` to allow localhost:5173 (frontend dev server)

---

*Architecture analysis: 2026-02-26*
