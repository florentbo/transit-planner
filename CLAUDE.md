# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Transit Planner is a full-stack application for managing personal transit routes across multiple cities (Brussels, London) with transit providers (STIB/MIVB, SNCB/NMBS, TfL). Currently in MVP stage with in-memory storage.

## Build & Development Commands

### Backend (Java 25 + Spring Boot 4.0)

```bash
cd backend
./gradlew build          # Compile, generate OpenAPI stubs, run tests
./gradlew compileJava    # Compile only (triggers OpenAPI generation)
./gradlew test           # Run tests only
./gradlew clean          # Clean build artifacts
```

The backend runs on port 8080.

### Frontend (React 19 + TypeScript + Vite)

```bash
cd frontend
npm install              # Install dependencies
npm run dev              # Start dev server on port 5173
npm run build            # TypeScript check + production build
npm run lint             # ESLint validation
npm run preview          # Preview production build
```

## Architecture

### Contract-First API Design

The system uses OpenAPI 3.0 contract-first approach:
- **Spec location**: `api-spec/openapi.yaml`
- **Backend**: OpenAPI Generator (v7.10.0) auto-generates REST interfaces in `build/generated/src/main/java`
- **Frontend**: Generated TypeScript/Axios client in `frontend/src/api/generated/`

When modifying APIs, update `api-spec/openapi.yaml` first, then regenerate both clients.

### Backend: DDD + Hexagonal Architecture

```
com.transit/
├── controller/          # REST API (implements OpenAPI-generated interfaces)
├── service/             # Business logic layer
└── config/              # Spring configuration (CORS, etc.)
```

**Key patterns**:
- Controllers implement OpenAPI-generated delegate interfaces
- Domain defines ports (interfaces), infrastructure implements adapters
- Aggregates: User (userId, clerkExternalId, email, preferredCity), SavedRoute (routeId, userId, name, origin, destination, transportOptions)
- Value Objects: Location, Stop, Line, Direction, TransitProvider, TransportMode, City

**Target package structure** (from ARCHITECTURE-DECISIONS.md):
- `domain/` - Pure business logic, zero framework dependencies
- `application/` - Use cases and ports
- `infrastructure/` - Framework-specific implementations (adapters)

### Frontend: Clean Architecture

```
frontend/src/
├── App.tsx              # Main component
├── main.tsx             # React entry point
├── components/          # UI components (CreateRouteForm, RouteList)
└── api/
    ├── config.ts        # API base URL configuration
    └── generated/       # OpenAPI-generated Axios client
```

**Target structure** (from ARCHITECTURE-DECISIONS.md):
- `entities/` - Pure TypeScript types
- `use-cases/` - Application business logic with port interfaces
- `infrastructure/` - API clients, auth adapters
- `presentation/` - React components and hooks

For React best practices and performance patterns, follow `frontend/REACT_RULES.md`.

## Current MVP State

- In-memory storage using ConcurrentHashMap (RouteService)
- Two endpoints: `POST /api/routes` (create), `GET /api/routes` (list)
- CORS configured for localhost:5173
- No authentication validation (JWT from Clerk defined but not enforced)

## Git Rules

- **Never commit without asking first**
- Use one-line commit messages only
- Do not mention Claude in commit messages
- Before committing, run tests for touched areas with limited output:
  - Frontend changes: `cd frontend && npm run lint && npm run build 2>&1 | tail -20`
  - Backend changes: `cd backend && ./gradlew test 2>&1 | tail -20`

## Key Files

- `api-spec/openapi.yaml` - API contract (source of truth)
- `backend/build.gradle` - Backend build config with OpenAPI Generator plugin
- `backend/src/main/resources/application.yml` - Spring config (port 8080)
- `frontend/src/api/config.ts` - API base URL (http://localhost:8080)
- `ARCHITECTURE.md` - High-level architecture overview
- `ARCHITECTURE-DECISIONS.md` - Detailed ADRs with domain model and patterns
