# Web Design Guidelines

UI/UX quality rules for this project, adapted from [Vercel's Web Interface Guidelines](https://github.com/vercel-labs/web-interface-guidelines).

## 1. Accessibility (CRITICAL)

- Icon-only buttons need `aria-label`
- Form controls need `<label>` or `aria-label`
- Interactive elements need keyboard handlers (`onKeyDown`/`onKeyUp`)
- `<button>` for actions, `<a>`/`<Link>` for navigation (not `<div onClick>`)
- Images need `alt` (or `alt=""` if decorative)
- Decorative icons need `aria-hidden="true"`
- Async updates (toasts, validation) need `aria-live="polite"`
- Use semantic HTML (`<button>`, `<a>`, `<label>`, `<table>`) before ARIA
- Headings hierarchical `<h1>`–`<h6>`; include skip link for main content
- `scroll-margin-top` on heading anchors

## 2. Focus States (HIGH)

- Interactive elements need visible focus: `focus-visible:ring-*` or equivalent
- Never `outline-none` without focus replacement
- Use `:focus-visible` over `:focus` (avoid focus ring on click)
- Group focus with `:focus-within` for compound controls

## 3. Forms (HIGH)

- Inputs need `autocomplete` and meaningful `name`
- Use correct `type` (`email`, `tel`, `url`, `number`) and `inputmode`
- Never block paste (`onPaste` + `preventDefault`)
- Labels clickable (`htmlFor` or wrapping control)
- Disable spellcheck on emails, codes, usernames (`spellCheck={false}`)
- Checkboxes/radios: label + control share single hit target (no dead zones)
- Submit button stays enabled until request starts; spinner during request
- Errors inline next to fields; focus first error on submit
- Placeholders end with `…` and show example pattern
- `autocomplete="off"` on non-auth fields to avoid password manager triggers
- Warn before navigation with unsaved changes (`beforeunload` or router guard)

## 4. Animation (MEDIUM)

- Honor `prefers-reduced-motion` (provide reduced variant or disable)
- Animate `transform`/`opacity` only (compositor-friendly)
- Never `transition: all` — list properties explicitly
- Set correct `transform-origin`
- Animations interruptible — respond to user input mid-animation

## 5. Typography (MEDIUM)

- `…` not `...`
- Curly quotes `"` `"` not straight `"`
- Non-breaking spaces: `10&nbsp;MB`, `⌘&nbsp;K`, brand names
- Loading states end with `…`: `"Loading…"`, `"Saving…"`
- `font-variant-numeric: tabular-nums` for number columns/comparisons
- Use `text-wrap: balance` or `text-pretty` on headings (prevents widows)

## 6. Content Handling (MEDIUM)

- Text containers handle long content: `truncate`, `line-clamp-*`, or `break-words`
- Flex children need `min-w-0` to allow text truncation
- Handle empty states — don't render broken UI for empty strings/arrays
- User-generated content: anticipate short, average, and very long inputs

## 7. Images (MEDIUM)

- `<img>` needs explicit `width` and `height` (prevents CLS)
- Below-fold images: `loading="lazy"`
- Above-fold critical images: `priority` or `fetchpriority="high"`

## 8. Performance (MEDIUM)

- Large lists (>50 items): virtualize (`virtua`, `content-visibility: auto`)
- No layout reads in render (`getBoundingClientRect`, `offsetHeight`, etc.)
- Batch DOM reads/writes; avoid interleaving
- Prefer uncontrolled inputs; controlled inputs must be cheap per keystroke
- Add `<link rel="preconnect">` for CDN/asset domains
- Critical fonts: `<link rel="preload" as="font">` with `font-display: swap`

## 9. Navigation & State (MEDIUM)

- URL reflects state — filters, tabs, pagination in query params
- Links use `<a>`/`<Link>` (Cmd/Ctrl+click, middle-click support)
- Deep-link all stateful UI (if uses `useState`, consider URL sync via `nuqs` or similar)
- Destructive actions need confirmation modal or undo window — never immediate

## 10. Touch & Interaction (LOW-MEDIUM)

- `touch-action: manipulation` (prevents double-tap zoom delay)
- `overscroll-behavior: contain` in modals/drawers/sheets
- During drag: disable text selection, `inert` on dragged elements
- `autoFocus` sparingly — desktop only, single primary input; avoid on mobile

## 11. Dark Mode & Theming (LOW-MEDIUM)

- `color-scheme: dark` on `<html>` for dark themes (fixes scrollbar, inputs)
- `<meta name="theme-color">` matches page background
- Native `<select>`: explicit `background-color` and `color` (Windows dark mode)

## 12. Content & Copy (LOW)

- Active voice: "Install the CLI" not "The CLI will be installed"
- Title Case for headings/buttons (Chicago style)
- Numerals for counts: "8 deployments" not "eight"
- Specific button labels: "Save API Key" not "Continue"
- Error messages include fix/next step, not just problem
- `&` over "and" where space-constrained

## 13. Visual Design Principles (MEDIUM)

Practical design rules for developers, adapted from [Refactoring UI](https://refactoringui.com/) by Adam Wathan (Tailwind creator) and Steve Schoger.

### Spacing
- Use a consistent spacing scale (Tailwind's default: 4, 8, 12, 16, 20, 24, 32, 40, 48, 64px)
- Start with too much whitespace, then remove — it's easier to identify what's missing than what's excess
- Use spacing and background color to group related elements before reaching for borders

### Color
- Define a constrained palette upfront — don't pick colors ad hoc
- Each color needs 8–10 shades (Tailwind provides this out of the box)
- On colored backgrounds, use same-hue lower-saturation text instead of white/gray
- Trust your eyes over math — adjust saturation/lightness after picking colors systematically
- Grays need a slight saturation tint (cool blue-gray or warm yellow-gray) to feel polished

### Typography
- Limit to 2 font families max (one for headings, one for body — or just one for both)
- Use font size and weight to create hierarchy, not color alone
- Fewer font sizes, more weight variation: normal (400) vs medium (500) vs semibold (600) vs bold (700)
- Line height: tighter for headings (1.2), looser for body text (1.5–1.75)
- Don't center long text — left-align body copy, center only short headings/labels

### Visual Hierarchy
- Primary actions get visual prominence (solid button), secondary actions recede (outline/ghost button)
- Bold text = more surface area = more emphasis — use weight strategically
- De-emphasize less important info with lighter color and smaller size, not just smaller size
- Design buttons based on hierarchy, not semantics — destructive actions don't always need red prominence
- Labels are secondary to values: make the data prominent, not the field name

### Depth & Elevation
- Use shadows for elevation, not borders — shadows feel more natural
- Smaller, tighter shadows for elements close to the surface (cards, dropdowns)
- Larger, more diffused shadows for elements floating higher (modals, popovers)
- Combine two shadows: a tight one for definition + a larger one for depth

## Anti-patterns Checklist

Flag these during review:

- [ ] `user-scalable=no` or `maximum-scale=1` disabling zoom
- [ ] `onPaste` with `preventDefault`
- [ ] `transition: all`
- [ ] `outline-none` without focus-visible replacement
- [ ] Inline `onClick` navigation without `<a>`
- [ ] `<div>` or `<span>` with click handlers (should be `<button>`)
- [ ] Images without dimensions
- [ ] Large arrays `.map()` without virtualization
- [ ] Form inputs without labels
- [ ] Icon buttons without `aria-label`
- [ ] Hardcoded date/number formats (use `Intl.*`)
- [ ] `autoFocus` without clear justification

## Related Guidelines

- [REACT_RULES.md](./REACT_RULES.md) — React performance patterns
- [COMPOSITION_PATTERNS.md](./COMPOSITION_PATTERNS.md) — Component architecture patterns
