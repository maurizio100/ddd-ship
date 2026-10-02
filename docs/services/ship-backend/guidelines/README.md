# ship-backend — guidelines

Conventions for `ship-backend`, in one place. **Each file states the rule as it is now — no dates, no
history, no ticket references.** Why a rule exists and when it changed belongs in
[`../decisions/`](../decisions/README.md); this folder only says what is true today.

| File | Covers |
|---|---|
| [`architecture.md`](architecture.md) | Hexagonal modules, the dependency rule, package layout, where invariants and transactions go |
| [`naming.md`](naming.md) | Names for ports, services, DTOs, value objects, controllers, HTTP models, JPA classes, events |
| [`persistence.md`](persistence.md) | Flyway migrations, table/column/ID conventions, JPA mapping, writing the outbox |
| [`api-design.md`](api-design.md) | REST resource layout under `/web`, response shape, `openapi.yml`, error mapping to Problem Details |
| [`testing.md`](testing.md) | Test levels and where they live, acceptance tests per Gherkin scenario, fixtures, isolation |

A rule here applies to the whole component. Only rules that are **persistent and non-obvious from
the code** belong here — not the name of one class, not something the next refactor changes.
