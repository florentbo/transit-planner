# React Composition Patterns

Component architecture patterns for building flexible, maintainable React components, adapted from [Vercel's agent-skills](https://github.com/vercel-labs/agent-skills/tree/main/skills/composition-patterns).

> Avoid boolean prop proliferation by using compound components, lifting state, and composing internals.

## 1. Avoid Boolean Prop Proliferation (CRITICAL)

Don't add boolean props like `isThread`, `isEditing` to customize behavior. Each boolean doubles possible states. Use composition instead.

```tsx
// ❌ Bad - exponential complexity
<Composer isThread isEditing={false} channelId="abc" showAttachments />

// ✅ Good - explicit variants
<ThreadComposer channelId="abc" />
<EditMessageComposer messageId="xyz" />
```

Each variant composes the pieces it needs — no hidden conditionals.

## 2. Compound Components (HIGH)

Structure complex components as compound components with shared context. Consumers compose the pieces they need.

```tsx
// ❌ Bad - monolithic with render props
<Composer
  renderHeader={() => <CustomHeader />}
  renderFooter={() => <Footer />}
  showAttachments
  showFormatting={false}
/>

// ✅ Good - compound components
<Composer.Frame>
  <Composer.Header />
  <Composer.Input />
  <Composer.Footer>
    <Composer.Formatting />
    <Composer.Emojis />
    <Composer.Submit />
  </Composer.Footer>
</Composer.Frame>
```

### Implementation pattern

```tsx
const ComposerContext = createContext<ComposerContextValue | null>(null)

function ComposerFrame({ children }: { children: React.ReactNode }) {
  return <form>{children}</form>
}

function ComposerInput() {
  const { state, actions: { update }, meta: { inputRef } } = use(ComposerContext)
  return (
    <TextInput
      ref={inputRef}
      value={state.input}
      onChangeText={(text) => update((s) => ({ ...s, input: text }))}
    />
  )
}

// Export as compound component
const Composer = {
  Frame: ComposerFrame,
  Input: ComposerInput,
  Submit: ComposerSubmit,
  // ...
}
```

## 3. Generic Context Interfaces (HIGH)

Define a generic interface with three parts: `state`, `actions`, `meta`. Any provider can implement this contract.

```tsx
interface ComposerContextValue {
  state: { input: string; attachments: Attachment[]; isSubmitting: boolean }
  actions: {
    update: (updater: (state: ComposerState) => ComposerState) => void
    submit: () => void
  }
  meta: { inputRef: React.RefObject<TextInput> }
}
```

UI components consume the interface, not the implementation:

```tsx
function ComposerInput() {
  const { state, actions: { update } } = use(ComposerContext)
  // Works with ANY provider that implements the interface
  return <TextInput value={state.input} onChangeText={(text) => update((s) => ({ ...s, input: text }))} />
}
```

## 4. Decouple State from UI (MEDIUM)

The provider is the only place that knows how state is managed. UI components don't know if state comes from `useState`, Zustand, or a server sync.

```tsx
// ❌ Bad - UI coupled to state implementation
function ChannelComposer({ channelId }: { channelId: string }) {
  const state = useGlobalChannelState(channelId)
  const { submit } = useChannelSync(channelId)
  return <Composer.Frame>...</Composer.Frame>
}

// ✅ Good - state isolated in provider
function ChannelProvider({ channelId, children }: Props) {
  const { state, update, submit } = useGlobalChannel(channelId)
  return (
    <Composer.Provider state={state} actions={{ update, submit }} meta={{ inputRef }}>
      {children}
    </Composer.Provider>
  )
}

// UI only knows the context interface
function ChannelComposer() {
  return (
    <Composer.Frame>
      <Composer.Input />
      <Composer.Submit />
    </Composer.Frame>
  )
}
```

Swap the provider, keep the UI.

## 5. Lift State into Providers (MEDIUM)

Move state into dedicated providers so sibling components outside the main UI can access it.

```tsx
// ❌ Bad - state trapped inside, siblings can't access
function ForwardMessageComposer() {
  const [state, setState] = useState(initialState)
  return <Composer.Frame>...</Composer.Frame>
}

// ✅ Good - state lifted to provider
function ForwardMessageProvider({ children }: { children: React.ReactNode }) {
  const [state, setState] = useState(initialState)
  const forwardMessage = useForwardMessage()
  return (
    <Composer.Provider state={state} actions={{ update: setState, submit: forwardMessage }}>
      {children}
    </Composer.Provider>
  )
}

// Siblings can access state and actions
function ForwardMessageDialog() {
  return (
    <ForwardMessageProvider>
      <Dialog>
        <ForwardMessageComposer />
        <MessagePreview />           {/* Reads composer state */}
        <ForwardButton />            {/* Calls submit action */}
      </Dialog>
    </ForwardMessageProvider>
  )
}
```

Components that need shared state don't have to be visually nested — they just need to be within the same provider.

## 6. Explicit Component Variants (MEDIUM)

Create explicit variant components instead of one component with many boolean props.

```tsx
// ❌ Bad - what does this render?
<Composer isThread isEditing={false} channelId="abc" showAttachments showFormatting={false} />

// ✅ Good - immediately clear
<ThreadComposer channelId="abc" />
<EditMessageComposer messageId="xyz" />
<ForwardMessageComposer messageId="123" />
```

Each variant is explicit about what provider/state it uses, what UI elements it includes, and what actions are available.

## 7. Children over Render Props (MEDIUM)

Use `children` for composition. Render props only when the parent needs to pass data back.

```tsx
// ❌ Render props - awkward
<Composer
  renderHeader={() => <CustomHeader />}
  renderFooter={() => <><Formatting /><Emojis /></>}
/>

// ✅ Children - flexible
<Composer.Frame>
  <CustomHeader />
  <Composer.Input />
  <Composer.Footer>
    <Composer.Formatting />
    <Composer.Emojis />
  </Composer.Footer>
</Composer.Frame>

// Render props OK when parent provides data
<List data={items} renderItem={({ item }) => <Item item={item} />} />
```

## 8. React 19 APIs (MEDIUM)

This project uses React 19. Use the new APIs.

```tsx
// ❌ Old - forwardRef
const Input = forwardRef<HTMLInputElement, Props>((props, ref) => {
  return <input ref={ref} {...props} />
})

// ✅ New - ref as regular prop
function Input({ ref, ...props }: Props & { ref?: React.Ref<HTMLInputElement> }) {
  return <input ref={ref} {...props} />
}

// ❌ Old - useContext
const value = useContext(MyContext)

// ✅ New - use (can be called conditionally)
const value = use(MyContext)
```

## 9. Component Organization: Atomic Design (MEDIUM)

Use [Brad Frost's Atomic Design](https://atomicdesign.bradfrost.com/) as a mental model for component hierarchy. Not a rigid folder structure — a way to think about UI at multiple levels simultaneously.

| Level | Description | Project Examples |
|-------|-------------|-----------------|
| **Atoms** | Basic HTML elements styled with Tailwind. Can't be broken down further. | Button, Input, Label, Icon, Badge |
| **Molecules** | Small groups of atoms working together as a unit. | Search bar (label + input + button), Form field (label + input + error) |
| **Organisms** | Complex UI sections composed of molecules and atoms. | Route card, Departure board, Create route form, Navigation header |
| **Templates** | Page-level layouts showing how organisms are arranged. No real data. | Dashboard layout, Route list layout, Route detail layout |
| **Pages** | Specific template instances with real data. | Home page, Routes page, Route detail for "Home to Work" |

**Key principle**: This is not a linear process. You can start at any level and work up or down. The value is thinking about your UI as both a cohesive whole and a collection of parts at the same time.

**Mapping to this project's structure:**
```
presentation/
├── components/
│   ├── common/          # Atoms + shared molecules (Button, Card, LoadingSpinner)
│   ├── routes/          # Organisms (RouteCard, RouteForm, TransportOptionList)
│   └── departures/      # Organisms (DepartureBoard, DepartureTime)
├── pages/               # Templates + Pages (HomePage, RoutesPage, RouteDetailPage)
└── hooks/               # Not part of Atomic Design — cross-cutting concerns
```

## Related Guidelines

- [REACT_RULES.md](./REACT_RULES.md) — React performance patterns
- [WEB_DESIGN_GUIDELINES.md](./WEB_DESIGN_GUIDELINES.md) — UI/UX quality rules
