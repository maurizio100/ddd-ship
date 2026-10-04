# 0001: Build the UI on Angular Material with a pirate theme

- **Status:** Proposed
- **Date:** 2026-10-04
- **Component:** ship-frontend

## Context
The UI is styled by about 800 lines of hand-written CSS with no component library, so every screen
reinvents buttons, cards, lists and forms, and a new screen (the harbor management page) would add
more. The redesign must keep the UI's charm — the Catain selection, the fleet layout, and Cargo
loaded by dragging it onto the ship — while looking more consistent and more pirate-themed.

## Decision
We will build the UI on Angular Material and the Angular CDK, with one custom pirate theme defined
through Material's theming (colour palette, typography, density) and shared design tokens; Cargo
drag-and-drop uses CDK drag-drop. Component CSS is limited to layout and to what the theme cannot
express.

## Consequences
- Screens share one set of components and one theme; a new page is assembled, not hand-styled.
- Drag-and-drop, focus handling and keyboard access come from the CDK instead of custom code.
- The look of the Catain selection and fleet layout must be rebuilt on Material components, which
  takes deliberate theming to stay distinctive rather than generic "Material".
- A large dependency joins the bundle and must stay within the `build:check` budget.
- Every existing screen is migrated; until then, old and new styles coexist.

## Alternatives considered
- **PrimeNG**: richer component set with ready themes. Rejected because it brings far more than the
  UI needs and a heavier dependency.
- **Tailwind CSS with the CDK**: utility classes, maximum visual freedom. Rejected because components
  stay home-made, which is the "not standardized" problem in a new form.
- **Own design system without a library**: tokens plus a few in-house components. Rejected because
  it keeps us maintaining our own buttons, dialogs and drag-and-drop.

## References
- docs/services/ship-frontend/guidelines/architecture.md
- docs/arc42/05-building-blocks.md (ship-frontend)
