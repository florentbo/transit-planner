# Technology Stack

**Analysis Date:** 2026-02-26

## Languages

**Primary:**
- Java 25 - Backend API implementation (`backend/src/main/java/com/transit/`)
- TypeScript 5.9.3 - Frontend application and type safety (`frontend/src/`)
- JavaScript (ES modules) - Frontend build configuration

**Secondary:**
- YAML - API specification and configuration (`api-spec/openapi.yaml`, `backend/src/main/resources/application.yml`)

## Runtime

**Environment:**
- Java Runtime Environment 25 (via Gradle toolchain configuration in `backend/build.gradle`)
- Node.js 20+ (specified in README, used for npm package management)

**Package Manager:**
- npm (Node Package Manager) - Frontend dependency management
- Gradle 9.2.1 - Backend build and dependency management
  - Lockfile: Gradle uses `.gradle/` directory for cached dependencies; no dedicated lockfile like package-lock.json
  - Uses Maven Central repository (`mavenCentral()`) and Spring milestone/snapshot repositories

## Frameworks

**Core:**
- Spring Boot 4.0.0 - REST API framework and application runtime (`backend/build.gradle`)
- React 19.2.0 - UI framework (`frontend/package.json`)
- Vite 7.2.4 - Frontend build tool and development server (`frontend/package.json`)

**API Code Generation:**
- OpenAPI Generator 7.10.0 - Generates Spring REST interfaces and TypeScript/React Query clients from OpenAPI spec
  - Backend: Generates `build/generated/src/main/java/com/transit/api/` with Spring delegate interfaces
  - Frontend: Generates `frontend/src/gen/` with Axios clients and React Query hooks via Kubb

**Testing:**
- Vitest 4.0.18 - Frontend unit test runner (`frontend/package.json`)
- Spring Boot Test (via `spring-boot-starter-test`) - Backend unit and integration testing
- JUnit Platform - Test execution for Spring Boot tests

**Styling:**
- Tailwind CSS 4.1.18 - Utility-first CSS framework (`frontend/package.json`)
- @tailwindcss/vite 4.1.18 - Vite integration for Tailwind

**Build/Dev:**
- @vitejs/plugin-react 5.1.1 - React JSX support in Vite
- ESLint 9.39.1 - JavaScript/TypeScript linting (`frontend/package.json`)
- TypeScript ESLint 8.46.4 - TypeScript-aware linting
- Gradle Wrapper 9.2.1 - Consistent Gradle version across environments

## Key Dependencies

**Critical:**
- spring-boot-starter-web 4.0.0 - Core Spring Web support for REST API
- springdoc-openapi-starter-webmvc-ui 2.7.0 - OpenAPI documentation and Swagger UI integration
- jakarta.validation:jakarta.validation-api 3.1.0 - Bean validation API for request DTOs
- swagger-annotations 2.2.26 - OpenAPI/Swagger annotations for API documentation
- jackson-databind-nullable 0.2.6 - Handles null values in generated models

**Frontend Infrastructure:**
- @tanstack/react-query 5.85.5 - Data fetching and state management for async server data
- @kubb/cli 4.12.8, @kubb/core 4.12.8 - Code generation tooling for OpenAPI
- @kubb/plugin-client 4.12.8 - HTTP client generation (Axios-based)
- @kubb/plugin-react-query 4.12.8 - React Query hook generation for API integration
- @kubb/plugin-ts 4.12.8, @kubb/plugin-oas 4.12.8 - TypeScript and OpenAPI schema support

**Development:**
- @testing-library/react 16.3.2, @testing-library/jest-dom 6.9.1 - React component testing utilities
- jsdom 28.1.0 - DOM implementation for Node.js test environment
- @types/react 19.2.5, @types/react-dom 19.2.3, @types/node 25.0.10 - TypeScript type definitions

**Utility:**
- globals 17.1.0 - Global object type definitions for Vitest

## Configuration

**Environment:**
- Backend: `backend/src/main/resources/application.yml` defines Spring Boot configuration
  - Server port: 8080
  - Application name: transit-backend
  - Logging levels: INFO for com.transit and org.springframework.web
  - CORS enabled for http://localhost:5173 (Vite dev server)

- Frontend: `frontend/vite.config.ts` configures development environment
  - Dev server: http://localhost:5173
  - API proxy: `/api` routes to http://localhost:8080
  - Test environment: jsdom with globals enabled

**Build:**
- Backend: `backend/build.gradle`
  - OpenAPI code generation runs before compile via `compileJava.dependsOn tasks.openApiGenerate`
  - Generated code: `build/generated/src/main/java`
  - JUnit Platform enabled for tests

- Frontend: `frontend/package.json` scripts
  - Verify step: TypeScript check (`tsc -b`) + ESLint + Vitest run
  - Build includes verification before production bundle

**API Contract:**
- Source of truth: `api-spec/openapi.yaml` (OpenAPI 3.0.3 specification)
- Defines request/response schemas, security schemes (bearer JWT), and HTTP operations
- Routes: POST `/api/routes` (create), GET `/api/routes` (list)

## Platform Requirements

**Development:**
- Java Development Kit 25 or compatible JDK
- Node.js 20+ with npm
- bash/zsh shell (gradle wrapper uses bash scripts)
- Gradle 9.2.1 (included via wrapper)

**Production:**
- Java Runtime Environment 25
- Node.js 20+ (for frontend build process)
- Deployment assumes containerization or VPS with above runtimes

**Browsers:**
- Modern browsers supporting ES2020+ (via Vite/Babel transpilation)
- React 19 Hydration API support

## Database Configuration

**Current State:**
- In-memory storage only (MVP): `backend/src/main/java/com/transit/service/RouteService.java` uses `ConcurrentHashMap`
- No database drivers or ORM dependencies installed

**Planned:**
- PostgreSQL listed in README.md as planned database
- No Spring Data JPA or database connection pool configured yet

---

*Stack analysis: 2026-02-26*
