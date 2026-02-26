# Coding Conventions

**Analysis Date:** 2026-02-26

## Naming Patterns

**Files:**
- React components: PascalCase with `.tsx` extension: `DashboardHeader.tsx`, `FavoriteRouteCard.tsx`
- TypeScript files: camelCase with `.ts` extension: `dashboard.ts`, `use-dashboard-service.ts`
- Hook files: kebab-case with `use` prefix: `useDashboard.ts`, `use-dashboard-service.ts`, `useDarkMode.ts`
- Utility files: kebab-case descriptive names: `greeting.ts`
- Test files: match source file with `.test.ts` suffix: `get-dashboard.test.ts`
- Java classes: PascalCase with domain-aware names: `RoutesApiController`, `RouteService`, `CorsConfig`
- Java packages: lowercase with domain separation: `com.transit.controller`, `com.transit.service`, `com.transit.config`

**Functions:**
- React components: PascalCase (exported as functions): `DashboardHeader()`, `FavoriteRouteCard()`
- Regular TypeScript functions: camelCase: `getGreeting()`, `getFormattedDate()`, `useDashboard()`
- React hooks: camelCase with `use` prefix: `useDarkMode()`, `useDashboardService()`
- Java methods: camelCase: `createRoute()`, `getAllRoutes()`, `addCorsMappings()`

**Variables:**
- React state and hooks: camelCase: `isDark`, `userInitial`, `favoriteRoute`
- Constants: UPPER_SNAKE_CASE for configuration, camelCase for computed constants
  - Example: `modeColors` (object), `variantStyles` (const as const)
- Type/interface names: PascalCase: `TransportLine`, `DashboardData`, `IDashboardService`
- Local variables in functions: camelCase: `mockData`, `colors`, `label`

**Types:**
- Type aliases: PascalCase with `type` keyword: `type TransportMode = 'METRO' | 'TRAM' | 'BUS' | 'TRAIN'`
- Interfaces: PascalCase with `I` prefix for service interfaces: `interface IDashboardService`
- Generic types use single-letter capitals: `T`, `K`, `V`

## Code Style

**Formatting:**
- Tool: ESLint (flat config) with TypeScript support
- Config: `frontend/eslint.config.js`
- Line length: No explicit limit enforced (modern monitors)
- Indentation: 2 spaces (implied by codebase)
- Trailing commas: Included for multiline structures

**Linting Rules:**
- `@typescript-eslint/no-unused-vars`: Error - but allows underscore-prefixed parameters (`argsIgnorePattern: '^_'`)
- `reactHooks/*`: Recommended rules for React hooks
- `react-refresh/only-export-components`: Enforces proper export patterns for Vite fast refresh
- TypeScript strict mode enabled with `noUnusedLocals`, `noUnusedParameters`, `noFallthroughCasesInSwitch`
- Linting command: `npm run lint`

**Build/Verify:**
- Frontend verify command: `npm run verify` - runs tests, ESLint, and TypeScript checks
- Backend verify command: `./gradlew test`

## Import Organization

**Order:**
1. React and external library imports
2. Type imports (marked with `type` keyword)
3. Relative imports from same or parent directories
4. Index/barrel imports from paths

**Pattern example** (`DashboardPage.tsx`):
```typescript
import { useDashboard } from '../hooks/useDashboard'
import { useDarkMode } from '../hooks/useDarkMode'
import { DashboardHeader } from '../components/DashboardHeader'
import { GreetingSection } from '../components/GreetingSection'
import { FavoriteRouteCard } from '../components/FavoriteRouteCard'
import { DarkModeToggle } from '../components/DarkModeToggle'
import { StarIcon } from '../components/StarIcon'
```

**Path Aliases:**
- No path aliases configured (direct relative imports used)
- Frontend uses direct relative imports throughout

## Error Handling

**Patterns:**
- React context usage: Explicit error throwing with descriptive messages: `throw new Error('ServiceProvider missing: wrap your app in <ServiceProvider>')`
- HTTP/API errors: Infrastructure handles errors (not explicitly shown in current code)
- Backend controller methods: Return ResponseEntity with appropriate HTTP status codes (e.g., `HttpStatus.CREATED`)
- No try-catch blocks visible in current codebase (suggests MVP-stage error handling)

## Logging

**Framework:** Console-based (browser console for frontend, standard JVM logging for backend)

**Patterns:**
- No explicit logging statements in current code
- Assumes error logging handled at framework level (Spring Boot, React)
- No centralized logger setup

## Comments

**When to Comment:**
- Minimal comments observed - code is self-documenting via clear naming
- Comments used only for non-obvious logic
- No JSDoc/TSDoc comments in current implementation

**Example of comment-free clarity:**
```typescript
const modeColors: Record<string, { bg: string; text: string; dot: string }> = {
  METRO: { bg: 'bg-orange-50 dark:bg-orange-900/30', ... },
  // Self-documenting through variable names
}
```

## Function Design

**Size:** Functions kept small and focused
- `useDarkMode()`: 20 lines - handles state initialization, effects, and return
- `GetDashboard.execute()`: 2 lines - delegates to service
- `DashboardPage()`: 35 lines - main page component with clear sections

**Parameters:**
- Destructured object props for components: `{ city, userInitial }`
- Explicit prop types in TypeScript: `{ route }: { route: FavoriteRoute }`
- Backend methods take domain objects: `SavedRouteRequest request`

**Return Values:**
- React components: Always return JSX
- Hooks: Return objects with `as const` for type safety: `return { isDark, toggle } as const`
- Service methods: Return domain objects or collections: `SavedRouteResponse`, `List<SavedRouteResponse>`

## Module Design

**Exports:**
- Default export for page components: `export default App`
- Named exports for reusable components: `export function DashboardHeader(...)`
- Named exports for hooks: `export function useDarkMode()`
- No wildcard exports

**Barrel Files:** Not used
- Direct imports from source files preferred (e.g., `import DashboardPage from './pages/DashboardPage'`)
- Reduces bundle size and improves tree-shaking

## Component Props Pattern

**Frontend Components:**
- Props destructured in function signature with inline type definition:
```typescript
export function DeparturePill({
  minutesUntil,
  variant = 'future',
}: {
  minutesUntil: number
  variant?: 'next' | 'future'
})
```
- Default values provided directly in destructuring when appropriate
- Type definitions placed inline as object type annotation

**Dependency Injection:**
- Constructor injection in TypeScript classes: `constructor(dashboardService: IDashboardService)`
- React context for runtime DI: `ServiceProvider` wraps app with `dashboardService` prop
- Hooks for accessing injected dependencies: `useDashboardService()` retrieves from context

## Clean Architecture Patterns

**Separation of Concerns:**
- `entities/`: Pure TypeScript types without framework dependencies
  - Example: `dashboard.ts` contains only type definitions
- `use-cases/`: Business logic classes that execute operations
  - Example: `GetDashboard` class with `execute()` method
- `infrastructure/`: Framework integration and external services
  - Example: `mock-dashboard-service.ts`, `service-provider.tsx` for DI
- `presentation/`: React components organized by type
  - Example: `components/` for reusable UI, `pages/` for page components, `hooks/` for custom hooks

**Backend Structure (targeting DDD):**
- `controller/`: REST API implementations (implements generated OpenAPI interfaces)
- `service/`: Business logic and domain operations
- `config/`: Spring configuration beans

## Tailwind CSS Patterns

**Usage:**
- Inline Tailwind classes in JSX: `className="bg-gradient-to-r from-indigo-600 to-purple-600"`
- Dark mode support: Use `dark:` prefix for dark theme: `dark:bg-gray-900`
- Responsive design: Mobile-first with `sm:`, `md:`, `lg:` breakpoints
- Accessibility consideration: `aria-hidden="true"` for decorative SVGs

**Example from `DashboardHeader.tsx`:**
```typescript
<header className="bg-gradient-to-r from-indigo-600 to-purple-600 dark:from-indigo-700 dark:to-purple-700">
  <div className="mx-auto flex max-w-2xl items-center justify-between px-4 py-4">
```

## TypeScript Configuration

**Key Settings:**
- Strict mode enabled: `"strict": true`
- Target: ES2022
- Module resolution: bundler (Vite)
- JSX: react-jsx (automatic runtime)
- `noUnusedLocals` and `noUnusedParameters` enforced to catch dead code
- `verbatimModuleSyntax` for explicit import/export syntax

---

*Convention analysis: 2026-02-26*
