# Transit Planner - Architecture Decision Document

## Overview
This document defines the architectural patterns, decisions, and structures for the Transit Planner application using **DDD + Hexagonal Architecture** (backend) and **Clean Architecture** (frontend), informed by industry best practices.

---

## 1. Backend Architecture: Domain-Driven Design + Hexagonal

### 1.1 Core Architectural Principles

**Hexagonal Architecture (Ports & Adapters)**
- **Application Core** contains domain and application layers (business logic)
- **Ports** are interfaces defining how to interact with the core
- **Adapters** are concrete implementations of ports (REST controllers, databases, external APIs)
- **Dependency Direction**: Always inward (Infrastructure → Application → Domain)

**Domain-Driven Design Tactical Patterns**
- **Aggregates**: Clusters of objects treated as a single unit with consistency boundaries
- **Entities**: Objects with unique identity that persists over time
- **Value Objects**: Immutable objects defined by their attributes, not identity
- **Domain Services**: Stateless operations that don't naturally fit in entities
- **Repositories**: Interfaces for aggregate persistence (defined in domain, implemented in infrastructure)

---

## 2. Backend: Domain Layer Design

### 2.1 Aggregate: User

**Aggregate Boundary Decision:**
- **Aggregate Root**: User entity
- **Identity**: UserId (value object, UUID-based)
- **External Reference**: ClerkExternalId (maps to Clerk auth system)
- **Value Objects**: Email, City (preferred city enum)
- **References to Other Aggregates**: SavedRoute IDs only (no direct object references)

**Invariants to Enforce:**
- Email must be valid format
- ClerkExternalId must be unique
- User can only exist with valid email

**Repository Port (Interface):**
- `findById(UserId)` - retrieve by internal ID
- `findByClerkId(ClerkExternalId)` - retrieve by external auth ID
- `save(User)` - persist user aggregate

**Rationale**: User is a separate aggregate from SavedRoute because:
- Users can exist without routes
- Route operations don't need to modify user state
- Different transaction boundaries needed

### 2.2 Aggregate: SavedRoute

**Aggregate Boundary Decision:**
- **Aggregate Root**: SavedRoute entity
- **Identity**: RouteId (value object, UUID-based)
- **Contained Entities**: TransportOption (cannot exist without parent route)
- **Value Objects**: Location, Stop, Line, Direction
- **References**: UserId (reference to User aggregate by ID only)

**Critical Design Decision: TransportOption is NOT an Aggregate Root**
- TransportOption is an entity within the SavedRoute aggregate
- Can only be accessed/modified through SavedRoute
- Lifecycle tied to SavedRoute (cascade delete)
- No repository needed for TransportOption alone

**Invariants to Enforce:**
- Must have at least one TransportOption (or allow zero for drafts - decision needed)
- Default option must exist in the transportOptions list
- Boarding and alighting stops must be different
- Origin and destination locations must be valid coordinates

**Repository Port (Interface):**
- `findById(RouteId)` - retrieve single route
- `findByUserId(UserId)` - retrieve all routes for a user
- `save(SavedRoute)` - persist route aggregate
- `delete(RouteId)` - remove route

**Rationale**: SavedRoute is an aggregate because:
- It represents a complete business concept
- TransportOptions have no meaning outside of a route
- All route modifications need transactional consistency

### 2.3 Value Objects Design

**Key Principle**: Value objects are **immutable** and compared by **value equality**

**Location Value Object:**
- Attributes: name (String), latitude (Decimal), longitude (Decimal)
- Validation: Coordinates within valid ranges (-90/90, -180/180)
- Immutability: All fields final, no setters
- Factory method for creation with validation

**Email Value Object:**
- Attributes: address (String)
- Validation: RFC 5322 email format
- Immutability: Cannot change once created

**Stop Value Object:**
- Attributes: stopId, name, provider
- Represents a physical transit stop
- Provider indicates which transit system (STIB, SNCB, TfL)

**Line Value Object:**
- Attributes: lineId, name, provider, color (optional)
- Represents a transit line (metro line 1, bus 71, etc.)

**Direction Value Object:**
- Attributes: directionId, terminus (destination station name)
- Represents the direction a line travels

**Stop, Line, Direction Rationale:**
- These are embedded within TransportOption
- No identity needed - defined by their attributes
- Immutable - transit system data doesn't change often
- Value equality - two stops with same ID are the same stop

### 2.4 Domain Services

**RouteMatcher Domain Service:**
- **Purpose**: Domain logic that doesn't naturally belong to an entity
- **Operation**: Find routes near a given location based on distance
- **Stateless**: No state, operates on passed-in aggregates
- **Pure Domain Logic**: No infrastructure dependencies

**When to Use Domain Services:**
- Logic involves multiple aggregates
- Operation doesn't conceptually belong to one entity
- Complex calculations or algorithms

---

## 3. Backend: Application Layer Design

### 3.1 Use Cases (Application Services)

**Purpose**: Orchestrate domain objects to fulfill user requests

**Responsibilities:**
- Receive input (commands/queries) from adapters
- Validate input data
- Load aggregates from repositories
- Delegate business logic to domain objects
- Persist changes via repositories
- Publish domain events
- Return results as DTOs

**Key Characteristics:**
- **Thin layer**: No business logic, only orchestration
- **Transaction boundary**: Each use case = one transaction
- **Depend on ports**: Use interfaces, not implementations
- **Technology-agnostic**: No framework dependencies in signatures

**Use Case Examples Needed:**

*User Context:*
- CreateUser - Handle user registration from Clerk webhook
- GetUserProfile - Retrieve current user data
- UpdateUserPreferences - Change preferred city

*Route Context:*
- CreateSavedRoute - Create new route with origin/destination
- GetUserRoutes - List all routes for a user
- AddTransportOption - Add transit option to existing route
- UpdateRouteDefaultOption - Set preferred transport option
- DeleteRoute - Remove a saved route

*Departures Context:*
- GetNextDepartures - Fetch real-time departure data

### 3.2 Ports (Interfaces)

**Inbound Ports (Use Case Interfaces):**
- Define what the application can do
- Called by inbound adapters (REST controllers)
- Return DTOs, not domain objects

**Outbound Ports (Repository & Service Interfaces):**
- Define what the application needs from outside world
- Implemented by outbound adapters (JPA, APIs, auth)

**Required Outbound Ports:**

*Repositories:*
- `UserRepository` - User aggregate persistence
- `SavedRouteRepository` - SavedRoute aggregate persistence

*External Services:*
- `RealTimeTransitProvider` - Interface for STIB/SNCB/TfL APIs
- `AuthProvider` - Interface for Clerk authentication
- `DomainEventPublisher` - Interface for event publishing

**Port Design Principle**: Application/Domain layers define interfaces, Infrastructure implements them (Dependency Inversion Principle)

### 3.3 Command/Query Objects

**Commands** (write operations):
- CreateSavedRouteCommand - contains userId, name, origin, destination
- AddTransportOptionCommand - contains routeId, provider, mode, stops, etc.

**Queries** (read operations):
- DepartureQuery - contains provider, stopId, lineId, direction

**DTOs** (responses):
- SavedRouteResponse - flattened representation for API
- DepartureInfo - next departure times and messages

**Rationale**: Separate request/response objects from domain objects to:
- Prevent domain model from being coupled to API
- Allow API to evolve independently
- Support multiple representations (REST, GraphQL, etc.)

---

## 4. Backend: Infrastructure Layer Design

### 4.1 Inbound Adapters

**REST Controllers (OpenAPI-Generated):**
- Implement OpenAPI-generated delegate interfaces
- Receive HTTP requests
- Map API DTOs to Commands/Queries
- Call use case inbound ports
- Map domain DTOs to API responses
- Handle HTTP concerns (status codes, headers)

**Delegate Pattern Decision:**
- Use OpenAPI generator's delegate pattern
- Generated code provides interface, we implement delegates
- Keeps our code separate from generated code

### 4.2 Outbound Adapters

**JPA Repository Implementations:**
- Implement domain repository ports
- Use Spring Data JPA for database access
- Map between domain aggregates and JPA entities
- Handle persistence concerns (transactions, locking)

**Mapping Strategy:**
- Domain Model ≠ Persistence Model
- Separate JPA entities from domain aggregates
- Mapper layer converts between them
- Allows domain model to evolve independently

**Transit Provider Adapters (STIB, SNCB, TfL):**
- Implement `RealTimeTransitProvider` port
- Call external transit APIs
- Map external responses to domain `DepartureInfo`
- Handle API-specific concerns (rate limiting, auth, retries)
- Act as Anti-Corruption Layer (ACL)

**Clerk Authentication Adapter:**
- Implement `AuthProvider` port
- Validate JWT tokens
- Extract user identity from token claims
- Map to domain User aggregate

**Adapter Pattern Benefit**: Easily swap implementations (e.g., switch from STIB to different provider, or Clerk to different auth)

---

## 5. Backend: Package Structure

### 5.1 Layered Package Organization

```
com.transit/
├── domain/                          # Pure business logic, zero framework dependencies
│   ├── user/                        # User bounded context
│   │   ├── User                     # Aggregate root
│   │   ├── UserId                   # Value object
│   │   ├── Email                    # Value object
│   │   └── UserRepository           # Port (interface)
│   ├── route/                       # Route bounded context
│   │   ├── SavedRoute               # Aggregate root
│   │   ├── TransportOption          # Entity (within aggregate)
│   │   ├── RouteId                  # Value object
│   │   ├── Location                 # Value object
│   │   ├── Stop                     # Value object
│   │   ├── Line                     # Value object
│   │   ├── Direction                # Value object
│   │   ├── SavedRouteRepository     # Port (interface)
│   │   └── RouteMatcher             # Domain service
│   └── shared/                      # Shared kernel
│       ├── TransitProvider          # Enum (STIB, SNCB, TFL)
│       ├── TransportMode            # Enum (METRO, TRAM, BUS, TRAIN)
│       └── City                     # Enum (BRUSSELS, LONDON)
│
├── application/                     # Use cases and ports
│   ├── user/
│   │   ├── CreateUserUseCase
│   │   ├── GetUserProfileUseCase
│   │   └── commands/                # Command DTOs
│   ├── route/
│   │   ├── CreateSavedRouteUseCase
│   │   ├── GetUserRoutesUseCase
│   │   ├── AddTransportOptionUseCase
│   │   └── commands/                # Command DTOs
│   ├── departures/
│   │   ├── GetNextDeparturesUseCase
│   │   └── queries/                 # Query DTOs
│   └── ports/                       # Outbound port interfaces
│       ├── RealTimeTransitProvider
│       ├── AuthProvider
│       └── DomainEventPublisher
│
└── infrastructure/                  # Framework-specific implementations
    ├── adapters/
    │   ├── inbound/                 # Driving adapters
    │   │   └── rest/
    │   │       ├── RouteController
    │   │       ├── UserController
    │   │       └── mappers/         # API DTO ↔ Command mappers
    │   └── outbound/                # Driven adapters
    │       ├── persistence/
    │       │   ├── JpaSavedRouteRepository
    │       │   ├── JpaUserRepository
    │       │   ├── entities/        # JPA entities (persistence model)
    │       │   └── mappers/         # Domain ↔ JPA mappers
    │       ├── transit/
    │       │   ├── StibTransitAdapter
    │       │   ├── SncbTransitAdapter
    │       │   └── TflTransitAdapter
    │       └── auth/
    │           └── ClerkAuthAdapter
    └── config/                      # Spring configuration
        ├── DomainConfiguration      # Wire up domain services
        ├── SecurityConfiguration    # Security setup
        └── PersistenceConfiguration # Database setup
```

### 5.2 Package Dependencies Rules

**Allowed Dependencies:**
- `infrastructure` → `application` → `domain`
- `application` → `domain`
- `domain` → nothing (pure Java)

**Forbidden Dependencies:**
- `domain` → `application` ❌
- `domain` → `infrastructure` ❌
- `application` → `infrastructure` ❌

**Enforcement:** Use ArchUnit tests to verify dependency rules at build time

---

## 6. Frontend Architecture: Clean Architecture

### 6.1 Core Principles

**Clean Architecture Layers (Frontend):**
- **Entities**: Pure TypeScript business objects and logic
- **Use Cases**: Application-specific business rules
- **Infrastructure**: Framework implementations (API calls, auth)
- **Presentation**: React components, hooks, UI logic

**Dependency Rule**: Same as backend - dependencies point inward

### 6.2 Entities Layer (Domain)

**Purpose**: Define business objects and validation logic

**Characteristics:**
- **Pure TypeScript**: No React, no frameworks, no external dependencies
- **Framework-agnostic**: Can be tested in isolation
- **Business logic**: Validation rules, domain calculations
- **Interfaces/Types**: Define shape of domain objects

**Entities Needed:**
- `Route` - matches backend SavedRoute
- `User` - matches backend User
- `TransportOption` - transport option details
- `Location` - coordinate and name
- `DepartureInfo` - real-time departure data

**Validation Logic:**
- RouteValidator - validate route has transport options
- LocationValidator - validate coordinates
- Business rules that don't depend on frameworks

**Rationale**: Entities layer represents "what the application knows" regardless of how it's presented or stored

### 6.3 Use Cases Layer

**Purpose**: Orchestrate business operations using entities

**Characteristics:**
- **Business workflows**: "Get saved routes", "Create route", "Get departures"
- **Depend on ports**: Use interfaces for external dependencies
- **Return domain objects**: Not framework-specific types
- **Testable**: Mock the ports, test business logic

**Use Cases Needed:**
- GetSavedRoutes - retrieve and filter user's routes
- CreateRoute - create new route with validation
- AddTransportOption - add option to existing route
- GetNextDepartures - fetch real-time data
- UpdateDefaultOption - change preferred transport

**Port Interfaces (defined in use-cases layer):**
- `IRouteRepository` - CRUD operations for routes
- `IAuthService` - authentication operations
- `ITransitService` - real-time transit data
- `IUserRepository` - user operations

**Rationale**: Use cases represent "what the application does" - the application's operations

### 6.4 Infrastructure Layer

**Purpose**: Implement ports using actual technologies

**Implementations:**

*API Repository:*
- Implements `IRouteRepository` port
- Uses generated OpenAPI TypeScript client
- Maps API responses to domain entities
- Handles HTTP errors and retries

*Clerk Auth Service:*
- Implements `IAuthService` port
- Wraps Clerk React SDK
- Provides authentication status
- Gets current user

*Transit Service:*
- Implements `ITransitService` port
- Calls backend API for real-time data
- Maps API response to domain `DepartureInfo`

**Dependency Injection:**
- Use React Context to provide implementations
- RepositoryProvider wraps app
- Hooks access repositories via context
- Easy to swap implementations for testing

**Rationale**: Infrastructure is "how the application does it" - the technical details

### 6.5 Presentation Layer

**Purpose**: React UI components and hooks

**Characteristics:**
- **React-specific**: Components, hooks, contexts
- **Depends on use cases**: Through dependency injection
- **UI logic only**: No business logic, only presentation concerns
- **Thin layer**: Delegates to use cases

**Custom Hooks Pattern:**
- `useSavedRoutes` - wraps GetSavedRoutes use case
- `useCreateRoute` - wraps CreateRoute use case
- `useDepartures` - wraps GetNextDepartures use case
- Hooks instantiate use cases with injected repositories
- Use React Query for caching and state management

**Component Organization:**
- Feature-based folders (routes/, departures/, user/)
- Shared components in common/
- Each feature has its own components
- Pages compose components

**Rationale**: Presentation is "what the user sees" - React-specific UI concerns

### 6.6 Frontend Package Structure

```
src/
├── entities/                        # Pure TypeScript domain
│   ├── route.ts                     # Route interface & types
│   ├── user.ts                      # User interface & types
│   ├── transport-option.ts          # TransportOption types
│   ├── departure-info.ts            # DepartureInfo types
│   └── validators/                  # Domain validation logic
│       ├── route-validator.ts
│       └── location-validator.ts
│
├── use-cases/                       # Application business logic
│   ├── ports/                       # Interfaces (dependency inversion)
│   │   ├── route-repository.ts      # IRouteRepository interface
│   │   ├── auth-service.ts          # IAuthService interface
│   │   ├── transit-service.ts       # ITransitService interface
│   │   └── user-repository.ts       # IUserRepository interface
│   ├── route/
│   │   ├── get-saved-routes.ts      # GetSavedRoutes use case
│   │   ├── create-route.ts          # CreateRoute use case
│   │   ├── add-transport-option.ts  # AddTransportOption use case
│   │   └── update-default-option.ts # UpdateDefaultOption use case
│   ├── departures/
│   │   └── get-next-departures.ts   # GetNextDepartures use case
│   └── user/
│       └── get-current-user.ts      # GetCurrentUser use case
│
├── infrastructure/                  # Framework implementations
│   ├── api/
│   │   ├── client.ts                # Generated OpenAPI client
│   │   ├── route-repository-impl.ts # Implements IRouteRepository
│   │   ├── transit-service-impl.ts  # Implements ITransitService
│   │   └── user-repository-impl.ts  # Implements IUserRepository
│   ├── auth/
│   │   └── clerk-auth-service.ts    # Implements IAuthService
│   └── di/
│       └── repository-provider.tsx  # DI container via React Context
│
└── presentation/                    # React UI layer
    ├── components/
    │   ├── routes/                  # Route-related components
    │   │   ├── RouteList.tsx
    │   │   ├── RouteCard.tsx
    │   │   ├── RouteForm.tsx
    │   │   └── TransportOptionList.tsx
    │   ├── departures/              # Departure-related components
    │   │   ├── DepartureBoard.tsx
    │   │   └── DepartureTime.tsx
    │   └── common/                  # Shared UI components
    │       ├── Button.tsx
    │       ├── Card.tsx
    │       └── LoadingSpinner.tsx
    ├── pages/
    │   ├── HomePage.tsx             # Landing/dashboard page
    │   ├── RoutesPage.tsx           # List all routes
    │   ├── RouteDetailPage.tsx      # Single route view
    │   └── CreateRoutePage.tsx      # Create route form
    ├── hooks/                       # React hooks wrapping use cases
    │   ├── use-saved-routes.ts      # Hook for GetSavedRoutes
    │   ├── use-create-route.ts      # Hook for CreateRoute
    │   ├── use-departures.ts        # Hook for GetNextDepartures
    │   └── use-auth.ts              # Hook for authentication
    └── App.tsx                      # Root component with providers
```

### 6.7 Frontend Dependency Rules

**Allowed Dependencies:**
- `presentation` → `use-cases` → `entities`
- `infrastructure` → `use-cases` → `entities`
- `presentation` → `infrastructure` (only for DI setup)

**Forbidden Dependencies:**
- `entities` → anything ❌
- `use-cases` → `infrastructure` ❌
- `use-cases` → `presentation` ❌
- `entities` → React ❌

**Enforcement**: TypeScript path mapping and eslint rules to prevent violations

---

## 7. Key Architectural Decisions

### 7.1 Aggregate Boundaries & Consistency

**Decision: User and SavedRoute are Separate Aggregates**

*Rationale:*
- **Lifecycle independence**: Users exist without routes, routes can be deleted without affecting users
- **Transaction boundaries**: Route operations don't need user modifications
- **Scalability**: Separate aggregates can be loaded/modified independently
- **Consistency needs**: Routes don't need immediate consistency with user state

*Implications:*
- Reference by ID only (SavedRoute contains userId, not User object)
- Can't navigate from User to SavedRoute in domain layer
- Different repositories for each aggregate
- Separate transactions when modifying each

**Decision: TransportOption is NOT an Aggregate Root**

*Rationale:*
- **Lifecycle dependency**: Options can't exist without a route
- **No independent identity**: Only meaningful in context of a route
- **Invariants span boundary**: Route must validate all its options
- **Transactional consistency**: All route changes must be atomic

*Implications:*
- TransportOption is an entity within SavedRoute aggregate
- Accessed only through SavedRoute
- No repository for TransportOption
- Deleted when parent route is deleted

### 7.2 Transaction Strategy

**Decision: One Use Case = One Transaction**

*Approach:*
- Each use case defines a transaction boundary
- Use Spring's `@Transactional` annotation
- Commit after use case completes successfully
- Rollback on any exception

*Cross-Aggregate Operations:*
- **Avoid multi-aggregate transactions** when possible
- Use **domain events** for cross-aggregate communication
- Accept **eventual consistency** between aggregates
- Example: RouteDeleted event can trigger analytics update separately

### 7.3 Consistency Model

**Decision: Strong Within, Eventual Between**

*Within Aggregate (Strong Consistency):*
- All invariants enforced immediately
- Atomic persistence of aggregate
- Single transaction per aggregate change
- Example: When setting default option, validate it exists in the list

*Between Aggregates (Eventual Consistency):*
- Domain events communicate changes
- Separate transactions for each aggregate
- Accept temporary inconsistencies
- Example: User preferences don't immediately affect routes

### 7.4 Persistence Strategy

**Decision: Aggregate = Table, Value Objects = Embedded Columns**

*Database Mapping:*
- Each aggregate root = one main table
- Contained entities = related tables with foreign keys
- Value objects = embedded columns (no separate table)
- Enums = string columns with constraints

*Schema Design:*

**User Aggregate:**
- `users` table
- Embedded: Email (email column), City (preferred_city column)
- Identity: UUID primary key
- External mapping: clerk_external_id unique constraint

**SavedRoute Aggregate:**
- `saved_routes` table (aggregate root)
- `transport_options` table (contained entities)
- Embedded in saved_routes: Location value objects (6 columns for origin + destination)
- Embedded in transport_options: Stop, Line, Direction value objects
- Foreign key: saved_routes.user_id → users.id
- Cascade delete: transport_options deleted when route deleted

**Indexes:**
- Primary keys on id columns (UUID)
- Index on saved_routes.user_id (frequent lookup)
- Index on transport_options.saved_route_id
- Unique constraint on users.clerk_external_id

*Rationale:*
- Value objects have no identity - embed them
- Entities within aggregates get separate tables
- Foreign keys enforce referential integrity
- Indexes optimize query patterns

### 7.5 API Contract Strategy

**Decision: Contract-First with OpenAPI 3.0**

*Approach:*
- Write OpenAPI spec first
- Generate server interfaces (Spring)
- Generate client SDK (TypeScript)
- Shared contract between frontend/backend

*Benefits:*
- Teams work in parallel
- Type safety on both sides
- Guaranteed API compatibility
- Documentation generated automatically

*Generation Strategy:*
- Backend: Generate delegate interfaces, implement manually
- Frontend: Generate TypeScript Axios client, wrap in repositories
- Keep generated code separate from business logic

### 7.6 Authentication & Authorization

**Decision: Clerk as Authentication Provider (Adapter)**

*Architecture:*
- Clerk is an **outbound adapter**
- Domain defines `AuthProvider` port (interface)
- Infrastructure implements via Clerk SDK
- Allows future provider swaps

*JWT Flow:*
- Frontend includes Clerk JWT in Authorization header
- Backend adapter validates token
- Extract user identity from `sub` claim
- Map to domain User via ClerkExternalId

*User Creation:*
- On first login, create User aggregate
- Map Clerk ID to internal UserId
- Store mapping in database
- Subsequent requests lookup via ClerkExternalId

### 7.7 Real-Time Transit Data Strategy

**Decision: Multiple Provider Adapters with Unified Port**

*Architecture:*
- `RealTimeTransitProvider` port (interface) in application layer
- Multiple implementations: StibAdapter, SncbAdapter, TflAdapter
- Each adapter translates provider-specific data to domain `DepartureInfo`

*Provider Selection:*
- Based on TransportOption.provider field
- Factory pattern or strategy pattern to route requests
- Each provider has own API key and configuration

*Anti-Corruption Layer:*
- Adapters isolate external API formats from domain
- Map provider responses to domain objects
- Handle provider-specific quirks (rate limits, auth)

### 7.8 Error Handling Strategy

**Decision: Domain Exceptions + HTTP Status Mapping**

*Domain Layer:*
- Define domain-specific exceptions
- Examples: InvalidEmailException, RouteNotFoundException
- Exceptions enforce invariants

*Application Layer:*
- Catch domain exceptions
- Return result objects or rethrow

*Infrastructure Layer:*
- Map domain exceptions to HTTP status codes
- 400 for validation errors (InvalidEmailException)
- 404 for not found (RouteNotFoundException)
- 403 for authorization failures
- 500 for unexpected errors

### 7.9 Testing Strategy

**Decision: Test at Each Layer Boundary**

*Domain Layer Tests:*
- Pure unit tests, no mocks needed
- Test aggregates, value objects, domain services
- Verify invariants are enforced
- Example: Test SavedRoute rejects invalid TransportOption

*Application Layer Tests:*
- Mock ports (repositories, external services)
- Test use case orchestration logic
- Verify commands produce expected results
- Example: Test CreateSavedRoute calls repository.save()

*Infrastructure Layer Tests:*
- Integration tests with real database (testcontainers)
- Test adapters against real external APIs (or mocks)
- Verify mapping between domain and persistence models
- Example: Test JpaRouteRepository correctly persists/retrieves

*Architecture Tests:*
- Use ArchUnit (backend) or eslint (frontend)
- Verify dependency rules
- Prevent forbidden imports
- Ensure layer isolation

### 7.10 Frontend Routing

**Decision: TanStack Router for Client-Side Routing**

*When to Add:* Feature 3 (Route Management) introduces `/routes/:id`, requiring a router.

*Approach:*
- Use TanStack Router for type-safe, client-first routing
- File-based route generation for automatic route discovery
- Integrates naturally with existing TanStack Query setup (loader/prefetch patterns)

*Rationale:*
- **Ecosystem consistency**: Already using TanStack Query via Kubb — shared patterns and conventions
- **Type-safe routes**: Route params, search params, and loaders are fully typed
- **Client-first**: No SSR complexity — fits this SPA + REST API architecture
- **Community momentum**: State of React 2025 survey shows TanStack Start/Router gaining significant traction as a lightweight alternative to Next.js

*Server Components — Intentionally Skipped:*
- State of React 2025 survey shows lukewarm adoption (described as "troubling" by survey authors)
- No benefit for SPA + REST API architecture
- Adds complexity without value for this use case

### 7.11 CSS & Component Library

**Decision: Tailwind CSS v4 + shadcn/ui**

*Current State:* Tailwind CSS v4 already integrated via `@tailwindcss/vite` plugin.

*Component Library:*
- Use shadcn/ui for pre-built, accessible UI components (dialogs, dropdowns, toasts, forms)
- Built on Radix UI primitives (unstyled, accessible) + Tailwind styling
- Copy-paste model: components are copied into the project, not installed as a dependency — full ownership and customization

*When to Add:* Feature 3+ (Route Management) when UI needs grow beyond basic forms — confirmation dialogs, toast notifications, dropdown menus.

*Rationale:*
- **Tailwind is industry standard**: State of React 2025 confirms utility-first CSS dominance; CSS-in-JS (Styled Components, Emotion) declining
- **shadcn/ui fastest-growing lib**: 20% → 56% adoption in two years (State of React 2025)
- **Accessible by default**: Radix UI primitives handle keyboard navigation, focus management, ARIA attributes
- **No vendor lock-in**: Copy-paste means no dependency on library releases or breaking changes
- **Perfect Tailwind fit**: Components use Tailwind classes natively, no style system mismatch

---

## 8. Strategic Design Decisions

### 8.1 Bounded Contexts

**Current System: Single Bounded Context**

*Justification:*
- Small domain with closely related concepts
- User, Route, TransportOption naturally belong together
- No conflicting models of same concepts
- Single team, single codebase

*Future Consideration:*
If system grows, consider splitting:
- **User Management Context** - user profiles, preferences, auth
- **Route Planning Context** - routes, options, favorites
- **Transit Data Context** - real-time data, schedules, providers

### 8.2 Multi-City Support

**Decision: Design for Multiple Cities, Start with Brussels**

*Architecture:*
- City enum in domain (BRUSSELS, LONDON)
- Provider enum includes all systems (STIB, SNCB, TfL)
- API accepts any provider
- User has preferredCity (optional)

*Implementation Plan:*
- Phase 1: Implement STIB adapter only
- Phase 2: Add SNCB adapter
- Phase 3: Add TfL adapter

*Rationale:*
- Architecture supports multiple cities
- Limit scope to one city initially
- Easier to add cities later

### 8.3 Caching Strategy

**Decision: No Cache for MVP, Add Later**

*Rationale:*
- Real-time data changes frequently (departures)
- User routes change infrequently (already in database)
- Premature optimization
- Add Redis later if needed for:
  - Session storage
  - Rate limiting
  - Departure data (short TTL)

### 8.4 Multi-Tenancy

**Decision: Shared Database with Tenant Filtering**

*Approach:*
- Single database for all users
- UserId foreign key in SavedRoute
- Filter by userId in all queries
- Row-level security if needed

*Alternatives Rejected:*
- Database per user: Too complex, harder to manage
- Separate schemas: Overkill for this domain

---

## 9. Implementation Roadmap

### Phase 1: Foundation (Backend Domain)
**Goal**: Establish core business logic

1. Create project structure following package layout
2. Define all value objects (Email, Location, Stop, Line, Direction)
3. Implement User aggregate with invariants
4. Implement SavedRoute aggregate with TransportOption entity
5. Define repository port interfaces
6. Write domain layer unit tests

**Success Criteria**: Domain layer compiles with zero framework dependencies

### Phase 2: Application Layer
**Goal**: Define use cases and ports

1. Define all command/query DTOs
2. Implement use cases (CreateRoute, GetUserRoutes, etc.)
3. Define outbound port interfaces (RealTimeTransitProvider, AuthProvider)
4. Write use case tests with mocked ports

**Success Criteria**: All use cases tested, no infrastructure dependencies

### Phase 3: API Contract
**Goal**: Establish frontend/backend contract

1. Write complete OpenAPI 3.0 specification
2. Generate Spring Boot server stubs
3. Generate TypeScript client
4. Define all endpoints, request/response schemas, error codes

**Success Criteria**: Both teams can work in parallel with generated code

### Phase 4: Backend Infrastructure
**Goal**: Wire up adapters

1. Implement JPA entities and repositories
2. Create database migration scripts
3. Implement REST controllers (OpenAPI delegates)
4. Implement Clerk authentication adapter
5. Implement STIB transit adapter
6. Wire up Spring configuration

**Success Criteria**: Backend runs end-to-end with real database and external APIs

### Phase 5: Frontend Foundation
**Goal**: Establish frontend architecture

1. Create project structure following package layout
2. Define all entity interfaces (matching backend)
3. Implement use cases (pure TypeScript)
4. Define port interfaces (IRouteRepository, IAuthService)
5. Setup dependency injection (React Context)

**Success Criteria**: Use cases tested without React dependencies

### Phase 6: Frontend Infrastructure
**Goal**: Connect to backend

1. Integrate generated OpenAPI client
2. Implement repository adapters (wrap client)
3. Implement Clerk auth service
4. Wire up DI container

**Success Criteria**: Can call backend APIs, authentication works

### Phase 7: Frontend Presentation
**Goal**: Build UI

1. Create React hooks wrapping use cases
2. Implement page components
3. Implement feature components
4. Add routing and navigation

**Success Criteria**: Full user journeys work end-to-end

---

## 10. Verification & Quality Assurance

### 10.1 Architecture Compliance

**Backend (ArchUnit Tests):**
- Domain layer has no Spring dependencies
- Domain doesn't import application or infrastructure
- Application doesn't import infrastructure
- All repositories are interfaces in domain
- Use cases depend only on ports

**Frontend (ESLint + TypeScript):**
- Entities don't import React
- Use cases don't import infrastructure
- Presentation doesn't import infrastructure (except DI)
- Path mappings enforce layer boundaries

### 10.2 Testing Pyramid

**Unit Tests (Most):**
- Domain logic (aggregates, value objects, services)
- Use cases (with mocked ports)
- Validators
- Mappers

**Integration Tests (Medium):**
- Repository implementations with database
- API controllers with mocked use cases
- External adapter integration

**E2E Tests (Least):**
- Critical user journeys
- Authentication flow
- Create route → add option → view departures

### 10.3 Code Quality Gates

- All tests pass
- Architecture tests pass
- Code coverage > 80% for domain and application layers
- No circular dependencies
- OpenAPI spec validates
- TypeScript strict mode enabled

---

## 11. Critical Success Factors

1. **Domain Model Integrity**
   - Aggregates enforce all invariants
   - Value objects are truly immutable
   - No framework contamination

2. **Dependency Direction**
   - All arrows point inward
   - Infrastructure knows about domain, never reverse
   - Ports define contracts, adapters implement

3. **Testability**
   - Domain testable without any infrastructure
   - Use cases testable with mocked ports
   - Fast test suite enables TDD

4. **Separation of Concerns**
   - Business logic in domain, never in controllers
   - Use cases orchestrate, don't contain logic
   - Adapters translate, don't make decisions

5. **Type Safety**
   - Value objects over primitives
   - Strong typing prevents invalid states
   - OpenAPI ensures frontend/backend compatibility

6. **Team Alignment**
   - Shared understanding of aggregates
   - Agreement on bounded contexts
   - Common ubiquitous language

---

## 12. Open Questions for Decision

1. **Invariant**: Should SavedRoute require at least one TransportOption, or allow zero for draft routes?
   - Recommend: Require at least one (simpler invariant)
   - Alternative: Allow drafts with flag

2. **User Creation**: Automatic on first login, or explicit registration API?
   - Recommend: Automatic via Clerk webhook
   - Store minimal data, update later

3. **Typical Duration**: Store in minutes? Calculated or user-provided?
   - Recommend: User-provided, optional field
   - Could calculate later from transit APIs

4. **Route Sharing**: Will users share routes? Public/private flag?
   - For MVP: All routes private
   - Future: Add visibility flag

5. **Multi-Device Sync**: Real-time or eventual consistency?
   - For MVP: Poll on page load
   - Future: WebSocket or SSE for real-time updates

---

## 13. Next Immediate Actions

**Step 1: Review this Architecture Document**
- Ensure all decisions align with team understanding
- Resolve open questions
- Get stakeholder buy-in

**Step 2: Create OpenAPI Specification**
- Define all endpoints, schemas, error responses
- Validate with frontend and backend teams
- Generate initial code

**Step 3: Start Backend Domain Layer**
- Set up project structure
- Implement value objects first (they're foundational)
- Then aggregates
- Then ports

**Step 4: Parallel Frontend Setup**
- While backend implements infrastructure
- Frontend team can work on entities and use cases
- Using mocked repositories

This architecture provides a solid foundation for a maintainable, testable, and scalable transit planning application.
