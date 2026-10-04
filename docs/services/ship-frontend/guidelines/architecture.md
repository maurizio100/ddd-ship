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

Shell-level concerns that belong to no feature (such as the Harbor Name in the app bar) live in
`src/app/core/`; features never import from it, and it imports only feature models.

Features import from each other only through models and the store (providers, selectors,
actions), never through another feature's components or services.

## Routing

### Application routes

- `app.routes.ts` lazy-loads each feature's `<Feature>Routing.ts` (`loadChildren`) or a single page
  (`loadComponent`). Feature routes register their store providers on the route (`providers:`).
- URLs follow the backend resource shape: `/ships`, `/ships/:id/cargo`,
  `/ships/:shipId/shipping/:shippingId`.

### Backend proxy

- The `/web` endpoint is proxied to the backend via nginx. The proxy is configured in
  `nginx/default.conf.template` and its upstream is set by the `BACKEND_URL` environment variable.
- All requests to `/web/...` are forwarded to the backend with forwarded headers (`Host`,
  `X-Forwarded-For`, `X-Forwarded-Proto`) to preserve the client's identity and protocol.
- `/web/fleet-events` (Server-Sent Events) has its own `location` before `location /web`: HTTP/1.1 with
  an empty `Connection` header, `proxy_buffering off`, `proxy_cache off` and `proxy_read_timeout 1h`.
  Buffered, events would reach the browser late or in bursts; with the default 60 s read timeout an
  idle stream would be cut. On k8s the Ingress `ddd-ship-fleet-events-ingress` does the same.

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
  in development). URLs are never hard-coded, including the Catain image URL, which derives from
  `environment.baseUrl`.
- The `production` build configuration in `angular.json` replaces `environment.ts` with
  `environment.prod.ts` through `fileReplacements`, so each Harbor's frontend calls its own backend
  through the nginx `/web` proxy. `npm run build:check` builds and then fails if the bundle still
  contains `localhost:8080`; the `Dockerfile` runs it.
- Services return `Observable`s from `HttpClient` and hold no state.

## Formatting

Prettier (configured in `package.json`): print width 100, single quotes, Angular parser for `.html`.
