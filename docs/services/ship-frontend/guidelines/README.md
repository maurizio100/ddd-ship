# ship-frontend — guidelines

Conventions for `ship-frontend`, in one place. **Each file states the rule as it is now — no dates, no
history, no ticket references.** Why a rule exists and when it changed belongs in
[`../decisions/`](../decisions/README.md); this folder only says what is true today.

| File | Covers |
|---|---|
| [`architecture.md`](architecture.md) | Feature folder layout, routing, component rules (standalone, `inject()`, `data-testid`), HTTP base URL, formatting |
| [`state-and-data.md`](state-and-data.md) | All server state through NgRx: action/effect/reducer/selector conventions, state shape, models |
| [`testing.md`](testing.md) | Karma/Jasmine specs per unit type, mock store and HTTP testing, fixtures, how stories are covered |

A rule here applies to the whole component. Only rules that are **persistent and non-obvious from
the code** belong here — not the name of one class, not something the next refactor changes.
