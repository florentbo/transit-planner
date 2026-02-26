# Testing Patterns

**Analysis Date:** 2026-02-26

## Test Framework

**Runner:**
- Vitest 4.0.18
- Config: `frontend/vite.config.ts`
- Test environment: jsdom (browser-like environment for React components)

**Assertion Library:**
- Vitest built-in assertions (no external library required)

**Backend Test Framework:**
- JUnit Platform (Spring Boot standard)
- Test runner specified in `build.gradle`: `useJUnitPlatform()`

**Run Commands:**
```bash
npm run test              # Run tests in watch mode (frontend)
npm run test:run         # Run tests once and exit (frontend)
npm run verify           # vitest run && eslint . && tsc -b (frontend)
./gradlew test           # Run backend tests (backend)
```

## Test File Organization

**Location:**
- Frontend: Co-located with source files
- Pattern: `[filename].test.ts` in same directory as implementation

**Example structure:**
```
frontend/src/
├── use-cases/
│   └── dashboard/
│       ├── get-dashboard.ts
│       └── get-dashboard.test.ts          ← Test co-located with implementation
├── entities/
│   └── dashboard.ts                       ← No tests currently
├── infrastructure/
│   └── ...                                ← No tests currently
└── presentation/
    └── ...                                ← No tests currently
```

**Naming:**
- Test file suffix: `.test.ts` (not `.spec.ts`)
- Mirrors source file name: `get-dashboard.ts` → `get-dashboard.test.ts`

**Backend Test Discovery:**
- Tests placed in `backend/src/test/java` (standard Maven/Gradle structure)
- No tests currently present in backend

## Test Structure

**Suite Organization (Vitest):**

From `frontend/src/use-cases/dashboard/get-dashboard.test.ts`:

```typescript
import { describe, it, expect } from 'vitest'
import { GetDashboard } from './get-dashboard'
import type { IDashboardService } from '../ports/dashboard-service'
import type { DashboardData } from '../../entities/dashboard'

describe('GetDashboard', () => {
  it('returns dashboard data from the service', () => {
    // Test implementation
  })

  it('returns the user name from the service', () => {
    // Test implementation
  })
})
```

**Patterns:**
- `describe()` creates test suite scoped to a single use case/function
- `it()` defines individual test cases with descriptive names
- Imports placed at top: test framework, implementation, types, mocks
- No setup/teardown hooks visible in current tests (simple pure functions)
- Tests are synchronous (no async patterns yet)

## Mocking

**Framework:** Manual object mocking (no external library)

**Pattern from `get-dashboard.test.ts`:**
```typescript
const mockDashboardData: DashboardData = {
  userName: 'Florent',
  city: 'Brussels',
  favoriteRoute: { ... }
}

it('returns dashboard data from the service', () => {
  const mockService: IDashboardService = {
    getDashboard: () => mockDashboardData,
  }
  const useCase = new GetDashboard(mockService)
  const result = useCase.execute()
  expect(result).toEqual(mockDashboardData)
})
```

**Approach:**
- Create mock object that implements the interface: `IDashboardService`
- Provide mock data as test constant: `mockDashboardData`
- Inject mock into class under test via constructor
- Verify behavior through assertions on return values

**What to Mock:**
- External service dependencies (interfaces/ports)
- Infrastructure layer implementations (API clients, databases)
- Things outside the system (time, random values, external APIs)

**What NOT to Mock:**
- Pure business logic
- Value objects
- Synchronous calculations
- Simple functions without side effects

## Fixtures and Factories

**Test Data:**
- Mock data defined as constants at test file level: `mockDashboardData`
- Reusable across multiple test cases in same file
- Data structure matches production domain model exactly

**Example fixture** (`get-dashboard.test.ts`):
```typescript
const mockDashboardData: DashboardData = {
  userName: 'Florent',
  city: 'Brussels',
  favoriteRoute: {
    id: '1',
    name: 'Home → Work',
    origin: 'Merode',
    destination: 'Schuman',
    lines: [
      {
        id: 'stib-m1',
        name: '1',
        mode: 'METRO',
        boardingStop: 'Merode',
        alightingStop: 'Schuman',
        direction: 'Stockel',
        departures: [
          { minutesUntil: 2, destination: 'Stockel' },
          { minutesUntil: 5, destination: 'Stockel' },
          { minutesUntil: 12, destination: 'Stockel' },
        ],
      },
    ],
  },
}
```

**Location:**
- Test fixtures co-located in same test file as their usage
- No separate fixtures directory currently
- Could be extracted to separate `__fixtures__` or `test-data.ts` file if reused across multiple test files

## Coverage

**Requirements:** Not enforced
- No coverage threshold configured in `vite.config.ts`
- No coverage reporting setup visible

**View Coverage:**
```bash
npm run test:run -- --coverage  # If coverage plugin installed (not currently configured)
```

**Current Status:**
- Only 1 test file exists: `get-dashboard.test.ts`
- Large portions of codebase untested (components, presentation layer, backend)
- Components and hooks have no test coverage currently

## Test Types

**Unit Tests:**
- Scope: Single use case or function in isolation
- Approach: Test class receives mocked dependencies, verifies output
- Example: `GetDashboard.execute()` test verifies it returns data from injected service
- Dependencies: Mocked via constructor injection
- No external calls (synchronous execution)

**Integration Tests:**
- Status: Not currently present
- Would test: Use case + real service implementations, React components + context
- Pattern: Not yet established (would need async patterns, fixtures, test setup)

**E2E Tests:**
- Status: Not configured
- Framework: Would likely use Playwright or Cypress if added
- Not planned in current MVP stage

## Common Patterns

**Synchronous Testing:**
```typescript
it('returns dashboard data from the service', () => {
  const mockService: IDashboardService = {
    getDashboard: () => mockDashboardData,
  }
  const useCase = new GetDashboard(mockService)
  const result = useCase.execute()
  expect(result).toEqual(mockDashboardData)
})
```

**Behavior Verification:**
```typescript
it('delegates to the injected service', () => {
  const customData: DashboardData = {
    ...mockDashboardData,
    userName: 'Alice',
    city: 'London',
  }
  const mockService: IDashboardService = {
    getDashboard: () => customData,
  }
  const useCase = new GetDashboard(mockService)

  expect(useCase.execute().userName).toBe('Alice')
  expect(useCase.execute().city).toBe('London')
})
```

**Assertion Patterns:**
- `expect(result).toEqual(mockDashboardData)` - deep equality check
- `expect(result.favoriteRoute.lines.length).toBeGreaterThan(0)` - property assertions
- `expect(useCase.execute().userName).toBe('Alice')` - specific value checks

## Test Execution in CI/CD

**Frontend Verify Command:**
```bash
cd frontend && npm run verify 2>&1 | tail -20
```
- Runs tests, linting, and TypeScript compilation together
- Output limited to last 20 lines for log readability
- All three checks must pass for clean build

**Backend Verify Command:**
```bash
cd backend && ./gradlew test 2>&1 | tail -20
```
- Runs only tests (linting/compilation handled separately in build.gradle)
- Output limited to last 20 lines

## Testing Guidelines

**When Writing Tests:**
1. Write test first (optional, but encouraged): test name describes expected behavior
2. Create realistic mock data that matches domain model
3. Test one behavior per test case (use separate `it()` blocks)
4. Use descriptive test names that explain "what" and "when": `returns dashboard data from the service`
5. Keep tests synchronous where possible (no async patterns yet established)
6. Inject dependencies via constructor for mockability
7. Assert on return values or side effects, not implementation details

**Test Naming Convention:**
- Verb-based: `returns`, `throws`, `delegates`, `validates`
- Complete phrase: `returns the user name from the service` not just `returns user name`
- Describes the condition: `when X happens, then Y result`

**Gaps to Address:**
- React components have no tests (`DashboardPage`, `DashboardHeader`, etc.)
- Hooks have no tests (`useDashboard`, `useDarkMode`, `useDashboardService`)
- Backend has no tests (controllers, services)
- No integration tests (use cases + real services)
- No E2E tests
- No component testing setup (React Testing Library installed but not used)

---

*Testing analysis: 2026-02-26*
