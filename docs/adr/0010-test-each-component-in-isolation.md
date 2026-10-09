# ADR-0010: Test each component in isolation

## Status
Accepted

## Date
2026-10-09

## Context
The ship-backend test suite starts a Postgres Testcontainer for the driven-adapter and acceptance
tests, and a Kafka Testcontainer for the messaging acceptance tests. Most stories are implemented by
a coding agent that runs `./mvnw verify` repeatedly. Each run is slow, writes long container logs that
the agent has to read, and needs a working Docker daemon. When Docker is missing or misbehaves, the
agent stalls or hangs. arc42 8.8 described the test levels only as a proposal, including an
`integration-test` module for Release → outbox → Debezium → Kafka that was declared in the parent
`pom.xml` but never built. CI (`.github/workflows/docker-image.yml`) builds the backend image and runs
no tests.

## Decision
We will test each component (ship-backend, ship-frontend) in isolation. A component's default test
command (`./mvnw verify`, `npm test`) needs no Docker, no network and no other component:

- **Backend acceptance tests** run over HTTP against the real Spring wiring, with in-memory fakes in
  place of the driven ports (persistence, outbox, Catain Images).
- **Postgres tests:** the driven-adapter tests against a real Postgres (Testcontainers) move to the
  opt-in Maven profile `-Pdb`. So do a few transaction tests that prove a failed change leaves neither
  its state change nor its outbox row behind. A new CI workflow runs `./mvnw verify -Pdb` on every
  pull request.
- **Kafka:** no Kafka Testcontainer. The listeners are tested with mocks in `driving-adapter`.
- **Cross-component tests:** none are automated. The `integration-test` module is dropped. The owner
  checks end-to-end behaviour by hand on the Compose stack.

## Consequences

### Positive
- The default suites are fast and quiet, and they cannot hang on a missing Docker environment.
- Acceptance tests describe behaviour through the driving ports, as the hexagonal layout (ADR-0001)
  intends, rather than through SQL.
- CI runs tests for the first time.

### Negative
- Adapter mistakes surface later. The native `ON CONFLICT` queries (inbox de-duplication ADR-0004,
  known Harbors ADR-0005, Stock upsert, Arrival), the Flyway migrations and the transaction tests run
  only under `-Pdb`, that is in CI, not in the agent's loop.
- The in-memory fakes are extra code. They can drift from the JPA adapters, for example in ordering,
  uniqueness or transaction rollback.
- Kafka consumer-group and read-from-the-beginning behaviour, a Harbor's first opening on a fresh
  database, and the outbox → Debezium → topic path (ADR-0002) have no automated test.

### Neutral
- Domain unit tests and driving-adapter tests are unchanged.
- ship-frontend is already isolated (Karma with a mock store, no HTTP); only its run command is aligned.

## Alternatives considered
- **Keep Testcontainers in the default build**: the status quo, rejected because of the cost and the
  hang risk described above.
- **H2 in PostgreSQL mode instead of a real Postgres**: rejected because H2 does not support the
  `ON CONFLICT` clauses the adapters rely on, so it would test a different database.
- **Drop the Postgres tests entirely**: rejected because inbox de-duplication and the atomic write of
  state change and outbox row are what make the messaging safe, and both are only verifiable against
  Postgres.
- **Build the `integration-test` module (Postgres, Kafka, Debezium)**: rejected because it is the most
  expensive and least stable kind of test, for a path that is quick to check by hand on Compose.

## References
- arc42: [08 Crosscutting](../arc42/08-crosscutting.md) (8.8), [11 Risks](../arc42/11-risks.md) (R-7)
- Guidelines: `docs/services/ship-backend/guidelines/testing.md`, `docs/services/ship-frontend/guidelines/testing.md`
- Related: ADR-0001, ADR-0002, ADR-0004, ADR-0005
