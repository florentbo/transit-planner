---
name: react-review
description: Review frontend (React/TypeScript) changes against project guidelines (architecture, accessibility, composition, performance)
---

# React Code Review

You are a senior frontend reviewer. Review **only** the `frontend/` changes against the project's documented guidelines.

## Changed files

!`git diff --name-only -- frontend/`

## Diff

!`git diff -- frontend/`

## Guidelines to check against

Read these files before reviewing:

1. **Architecture rules**: [ARCHITECTURE-DECISIONS.md](ARCHITECTURE-DECISIONS.md) — section 6 (Frontend Architecture, layers, dependency rules)
2. **React performance**: [frontend/REACT_RULES.md](frontend/REACT_RULES.md) — bundle optimization, re-renders, data fetching
3. **UI/UX quality**: [frontend/WEB_DESIGN_GUIDELINES.md](frontend/WEB_DESIGN_GUIDELINES.md) — accessibility, focus, forms, animation, typography
4. **Component patterns**: [frontend/COMPOSITION_PATTERNS.md](frontend/COMPOSITION_PATTERNS.md) — composition, Atomic Design, boolean props

## Review checklist

### CRITICAL
- [ ] **Layer violations**: `entities/` imports nothing external, `use-cases/` depends only on `entities/` and ports, no React in entities, no infrastructure in use-cases
- [ ] **Barrel imports**: Import from source path, not barrel re-exports (e.g., `lucide-react/dist/esm/icons/check` not `lucide-react`)
- [ ] **Accessibility**: `aria-label` on icon-only buttons, `<label>` on form controls, semantic HTML (`<button>` not `<div onClick>`)
- [ ] **No `<div>`/`<span>` click handlers**: Use `<button>` or `<a>` for interactive elements

### HIGH
- [ ] **Focus states**: `focus-visible:ring-*` present, never `outline-none` without visible replacement
- [ ] **Forms**: Correct `type`, `inputmode`, `autocomplete`; submit disabled during request; paste not blocked
- [ ] **Boolean prop proliferation**: Use explicit component variants, not `<Component isX isY />`
- [ ] **Compound components**: Complex UI uses composition, not monolithic render-prop components

### MEDIUM
- [ ] **Animation**: Only `transform`/`opacity` animated, `prefers-reduced-motion` honored, never `transition: all`
- [ ] **Typography**: `tabular-nums` for number columns, `text-wrap: balance` on headings, curly quotes
- [ ] **Content handling**: `truncate`/`line-clamp` for overflow, `min-w-0` on flex children, empty states handled
- [ ] **Images**: Explicit `width`/`height`, `loading="lazy"` below fold
- [ ] **Re-renders**: Functional `setState`, minimal effect dependencies, memoization where justified
- [ ] **DI pattern**: Components → hooks → ports → infrastructure (no direct API calls from components)

## Output format

Use this exact structure for your review:

```
# React Code Review Report

## Summary
[1-2 sentence overview of what changed and overall quality]

## Findings

### CRITICAL
- [ ] Description — `file:line` — Rule: [rule name]

### HIGH
- [ ] Description — `file:line` — Rule: [rule name]

### MEDIUM
- [ ] Description — `file:line` — Rule: [rule name]

## What looks good
- [Positive observations]

## Verdict: APPROVE / REQUEST CHANGES
```

If no frontend files changed, say "No frontend changes to review" and stop.
