# Transit Planner

A personal transit planning application for saving and quickly accessing frequent routes (home→work, home→pool, etc.) with real-time departure information.

## Features (Roadmap)

- **User Accounts** — Sign up/login via Clerk
- **Saved Routes** — Create, edit, delete personal routes
- **Transport Options** — Add metro, bus, train lines to routes
- **Live Departures** — Real-time "next train in 3 min" from transit APIs
- **Multi-City** — Brussels (STIB, SNCB) and London (TfL)

See [ROADMAP.md](ROADMAP.md) for the full feature roadmap.

## Tech Stack

| Layer | Technology |
|-------|------------|
| Frontend | React 19 + TypeScript + Vite |
| Backend | Java 25 + Spring Boot 4.0 |
| API Design | OpenAPI 3.0 (Contract-First) |
| Authentication | Clerk |
| Database | PostgreSQL (planned) |

## Getting Started

### Prerequisites

- Java 25+
- Node.js 20+
- PostgreSQL (optional, currently using in-memory storage)

### Backend

```bash
cd backend
./gradlew build    # Compile + generate OpenAPI stubs + run tests
./gradlew bootRun  # Start server on port 8080
```

### Frontend

```bash
cd frontend
npm install        # Install dependencies
npm run dev        # Start dev server on port 5173
```

### API Spec

The API contract is defined in `api-spec/openapi.yaml`. Both backend (Spring) and frontend (TypeScript/React Query) clients are auto-generated from this spec.

## Project Structure

```
transit-planner/
├── api-spec/           # OpenAPI 3.0 specification
├── backend/            # Java + Spring Boot
│   └── src/main/java/com/transit/
├── frontend/           # React + TypeScript + Vite
│   └── src/
├── ARCHITECTURE.md     # High-level architecture
├── ARCHITECTURE-DECISIONS.md  # Detailed ADRs
└── ROADMAP.md          # Feature roadmap
```

## License

MIT
