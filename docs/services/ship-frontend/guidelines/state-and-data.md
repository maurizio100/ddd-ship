# ship-frontend — state and data

## Server state goes through NgRx

Data from the backend always flows through a feature store:

```
component ──dispatch──▶ action ──▶ effect ──▶ service (HTTP)
    ▲                                 │
    └── store.selectSignal(selector) ◀─ reducer ◀─ success / failure action
```

- Components never call a service directly. They dispatch an action and read with
  `store.selectSignal(...)`.
- An effect is the only place that calls a service. It maps the result to a `… Success` action, or
  to a `… Failure` action that carries the error.
- Reducers are pure functions with no side effects or service calls. Every feature state carries
  `loading` and `error` next to its data.
- Local UI state (a form's input, an open dialog) stays in the component as Angular signals.
- Server pushes enter the same way. An `EventSource` is wrapped by a service that exposes it as an
  `Observable`, opening it on subscribe and closing it on unsubscribe, and builds it through the
  `EVENT_SOURCE_FACTORY` injection token so specs pass a fake. Only an effect subscribes, started and
  stopped by actions the component dispatches on init and destroy, and it maps each pushed event to an
  action.

Existing components read with `store.select(...)` as observables; switch one to `selectSignal`
when a story touches it. The `shipping` feature still calls its services from components. Move it onto a store when a story
changes that flow.

## Actions

- Action types are `[<Feature>] <Verb> <Noun>`, with `Success` / `Failure` variants for every
  request: `[Ship] Load Ships`, `[Ship] Load Ships Success`, `[Ship] Load Ships Failure`.
- Defined with `createAction` + `props<…>()` in `<feature>.actions.ts`.

## State keys and selectors

- The feature state is registered under a plural key (`ships`, `catains`, `shippings`), with its
  `<Feature>State` interface next to the selectors.
- Selectors are named `select<Thing>` and built with `createFeatureSelector` / `createSelector`.

## Models

- Models are TypeScript `interface`s that mirror the backend's JSON (`ship-backend/openapi.yml`),
  in the feature's `models/` folder: `Ship`, `Cargo`, `ShippingSummary`, and request bodies as
  `<X>Request`.
- Ids are the backend's UUID strings.
- Money arrives as a decimal string with two decimals (`"1000.00"`) and is shown as `<amount> $`. Never
  convert it to a number: a float would lose cents.
