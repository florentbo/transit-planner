# React Best Practices

Performance optimization rules for this Vite + React 19 project, adapted from [Vercel's agent-skills](https://github.com/vercel-labs/agent-skills/tree/main/skills/react-best-practices).

## 1. Bundle Optimization (CRITICAL)

### Avoid Barrel File Imports
Barrel files load entire module graphs unnecessarily. Import directly from source.

```tsx
// ❌ Bad - loads entire library
import { Check } from 'lucide-react'

// ✅ Good - direct import
import Check from 'lucide-react/dist/esm/icons/check'
```

### Dynamic Imports for Heavy Components
Use `React.lazy()` for components not needed on initial load.

```tsx
const HeavyChart = React.lazy(() => import('./HeavyChart'))

function Dashboard() {
  return (
    <Suspense fallback={<Loading />}>
      <HeavyChart />
    </Suspense>
  )
}
```

### Preload on User Intent
Preload components when user shows intent (hover, focus).

```tsx
const loadEditor = () => import('./Editor')

<button onMouseEnter={loadEditor} onClick={() => setShowEditor(true)}>
  Edit
</button>
```

## 2. Re-render Optimization (MEDIUM)

### Extract to Memoized Components
Move expensive work to separate `memo()` components for early returns.

```tsx
// ❌ Bad - computation runs even during loading
function Parent({ isLoading }) {
  const expensive = useMemo(() => compute(), [])
  if (isLoading) return <Loading />
  return <div>{expensive}</div>
}

// ✅ Good - computation skipped during loading
const ExpensiveChild = memo(function ExpensiveChild() {
  const expensive = compute()
  return <div>{expensive}</div>
})

function Parent({ isLoading }) {
  if (isLoading) return <Loading />
  return <ExpensiveChild />
}
```

### Functional setState
Avoid dependencies by using functional updates.

```tsx
// ❌ Bad - depends on items
const addItem = useCallback((newItem) => {
  setItems([...items, newItem])
}, [items])

// ✅ Good - no dependencies
const addItem = useCallback((newItem) => {
  setItems(curr => [...curr, newItem])
}, [])
```

### Lazy State Initialization
Use initializer function for expensive initial values.

```tsx
// ❌ Bad - parses on every render
const [data, setData] = useState(JSON.parse(localStorage.getItem('data')))

// ✅ Good - parses once
const [data, setData] = useState(() => JSON.parse(localStorage.getItem('data')))
```

### Narrow Effect Dependencies
Only include what's actually used in the effect.

```tsx
// ❌ Bad - reruns when any user field changes
useEffect(() => {
  fetchData(user.id)
}, [user])

// ✅ Good - only reruns when id changes
useEffect(() => {
  fetchData(user.id)
}, [user.id])
```

## 3. Client-Side Data Fetching (MEDIUM-HIGH)

### Consider SWR or React Query
Automatic deduplication, caching, and revalidation.

```tsx
// ❌ Manual - no dedup, no cache
useEffect(() => {
  fetch('/api/routes').then(r => r.json()).then(setRoutes)
}, [])

// ✅ SWR - automatic dedup and cache
const { data: routes } = useSWR('/api/routes', fetcher)
```

### Deduplicate Event Listeners
Single global listener instead of per-component.

```tsx
// ❌ Bad - each component adds listener
useEffect(() => {
  window.addEventListener('resize', handler)
  return () => window.removeEventListener('resize', handler)
}, [])

// ✅ Good - shared hook with single listener
const windowSize = useWindowSize() // single global listener
```

### Passive Event Listeners
For scroll/touch handlers that don't call preventDefault.

```tsx
element.addEventListener('scroll', handler, { passive: true })
```

## 4. Rendering Performance (MEDIUM)

### Explicit Conditional Rendering
Use early returns for cleaner code and better performance.

```tsx
// ❌ Bad - ternary nesting
return (
  <div>
    {isLoading ? <Loading /> : error ? <Error /> : <Content />}
  </div>
)

// ✅ Good - early returns
if (isLoading) return <Loading />
if (error) return <Error />
return <Content />
```

### Hoist Static JSX
Move static elements outside the component.

```tsx
// ❌ Bad - recreated every render
function Component() {
  const header = <header>Static Header</header>
  return <div>{header}</div>
}

// ✅ Good - created once
const Header = <header>Static Header</header>

function Component() {
  return <div>{Header}</div>
}
```

### useTransition for Non-Urgent Updates
Keep UI responsive during expensive updates.

```tsx
const [isPending, startTransition] = useTransition()

const handleSearch = (query) => {
  startTransition(() => {
    setSearchResults(filterLargeList(query))
  })
}
```

## 5. JavaScript Performance (LOW-MEDIUM)

### Early Return
Exit functions as soon as possible.

```tsx
// ❌ Bad
function process(items) {
  if (items.length > 0) {
    // ... lots of code
  }
}

// ✅ Good
function process(items) {
  if (items.length === 0) return
  // ... lots of code
}
```

### Use Set/Map for Lookups
O(1) vs O(n) for repeated lookups.

```tsx
// ❌ Bad - O(n) each lookup
const isSelected = (id) => selectedIds.includes(id)

// ✅ Good - O(1) lookups
const selectedSet = new Set(selectedIds)
const isSelected = (id) => selectedSet.has(id)
```

### Cache Repeated Function Calls
Store results used multiple times.

```tsx
// ❌ Bad - calls twice
if (expensiveCheck() && otherCondition) {
  doSomething(expensiveCheck())
}

// ✅ Good - call once
const result = expensiveCheck()
if (result && otherCondition) {
  doSomething(result)
}
```

## Notes

- **React 19**: May include React Compiler which auto-optimizes memoization
- **This is Vite**: No Server Components, Server Actions, or Next.js-specific patterns
- **Priority**: Focus on Bundle and Client-Side Fetching first, micro-optimizations last

## Related Guidelines

- [WEB_DESIGN_GUIDELINES.md](./WEB_DESIGN_GUIDELINES.md) — UI/UX quality rules (accessibility, forms, animation, typography, dark mode)
- [COMPOSITION_PATTERNS.md](./COMPOSITION_PATTERNS.md) — Component architecture patterns (compound components, state lifting, providers)
