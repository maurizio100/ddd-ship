# ship-frontend — testing

**Run:** `cd ship-frontend && npm test -- --watch=false --browsers=ChromeHeadless`
(plain `npm test` watches). The specs run in CI (`.github/workflows/test.yml`) on every pull
request and push to `main`.

Karma + Jasmine through the Angular CLI. A spec sits next to the file it tests, as `<file>.spec.ts`.

## What to test, and how

| Unit | Approach |
|---|---|
| Reducer | Pure function: call it with an initial state and an action, then assert on the new state. No TestBed. |
| Selector | Pure function: call `selectX.projector(…)` or pass a hand-built root state. |
| Effect | TestBed with `provideMockActions(() => actions$)` and a Jasmine spy for the service. Assert on the emitted success/failure action. |
| Service | TestBed with `provideHttpClient()` + `provideHttpClientTesting()`. Assert on URL, method and body with `HttpTestingController`. |
| Component | TestBed with the standalone component in `imports` and `provideMockStore({ initialState })`. Find elements by `data-testid`, and assert on rendered text and on dispatched actions (`spyOn(store, 'dispatch')`). |

- A component spec never hits HTTP; it works against the mock store.
- Test data comes from builder functions named after the domain term: `aShip()`, `aCargo()`,
  `aCatain()`, with every field defaulted. They live in `src/testing/fixtures.ts`.
- There is no e2e suite. Story acceptance is tested against the backend API
  (see [`ship-backend` testing](../../ship-backend/guidelines/testing.md)). A frontend story is covered
  by component specs, one `it(...)` per scenario that has a UI outcome, named after the scenario.
