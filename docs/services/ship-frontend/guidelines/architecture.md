# ship-frontend — architecture

## Layout

The app is split into feature folders under `src/app/features/`: `ships` (the ship list and
creating ships, which is Fleet), `catains` (the roster, Fleet), and `shipping` (cargo loading,
Release and the Shipping Summary, which is CargoLoading + Shipping). Each feature has the same
shape:

```
features/<feature>/
  components/<name>/<name>.component.{ts,html,css}
  models/<name>.ts              interfaces mirroring the /web JSON
  services/<name>.service.ts    HttpClient calls only
  store/
    <feature>.actions.ts
    <feature>.reducers.ts
    <feature>.selectors.ts
    <feature>.effects.ts
    <Feature>StoreProviders.ts  provideState + provideEffects for the feature
  <Feature>Routing.ts           the feature's routes
```

Features import from each other only through models and the store (providers, selectors,
actions), never through another feature's components or services.

## Routing

- `app.routes.ts` lazy-loads each feature's `<Feature>Routing.ts` (`loadChildren`) or a single page
  (`loadComponent`). Feature routes register their store providers on the route (`providers:`).
- URLs follow the backend resource shape: `/ships`, `/ships/:id/cargo`,
  `/ships/:shipId/shipping/:shippingId`.

## Components

- Standalone components only, with no NgModules. Dependencies are injected with `inject()`, not
  through constructor parameters.
- Selector prefix `app-`. The class is `<Name>Component` in `<name>.component.ts`, with a separate
  `.html` and `.css` file.
- Any element a test or user action targets gets `data-testid="<feature>-<element>"` in kebab-case,
  e.g. `data-testid="shipping-release-button"`.
- UI text and new names use the canonical terms from [`docs/domain/`](../../../domain/README.md):
  "Catain", and "Release" rather than "Disembark" (an alias that remains in the existing
  `disembark-*` names).

## HTTP

- The base URL comes from `environment.baseUrl` (`/web` in production, `http://localhost:8080/web`
  in development). URLs are never hard-coded.
- Services return `Observable`s from `HttpClient` and hold no state.

## Formatting

Prettier (configured in `package.json`): print width 100, single quotes, Angular parser for `.html`.
