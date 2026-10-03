> Status: draft

# 8. Crosscutting Concepts

## 8.1 Domain model and language

The ubiquitous language lives in [`../domain/`](../domain/README.md) and is used verbatim in code,
including the "Catain" spelling. Identity is carried by UUID value objects (`ShipId`, `CargoId`,
`ShippingId`, `CatainId`); persistence entities additionally have a numeric surrogate key. Planned
([ADR-0003](../adr/0003-run-each-ship-backend-instance-as-one-harbor.md)): the UUID `ShipId` is the
ship's identity across all Harbors and travels in events; the surrogate key never leaves its Harbor.

## 8.2 Persistence

- PostgreSQL via Spring Data JPA in `driven-adapter`; domain objects are mapped to separate `*PersistenceEntity` classes, so JPA never leaks into `domain`.
- The schema is owned by **Flyway** (`application/src/main/resources/db/migration`, `V1__init.sql` …); Hibernate DDL generation is off (`ddl-auto: none`).
- Reference data (Cargo catalog, Catain roster, Shipping Quotes) is seeded by migrations `V2`–`V4`.
- Binary data (Catain Images) lives in MinIO, not in the database.
- Each Harbor's **Stock** lives in its own database, in `stocks` (one row per catalog Cargo). `V7__stocks.sql` seeds the Starting Stock (3 of every Cargo); Flyway applies it once per database, i.e. when the Harbor opens for the first time. The Stock is changed only through `StockRepositoryPort`, inside the caller's transaction, and never drops below 0 (a conditional `UPDATE … WHERE stock_quantity > 0` backed by a `CHECK` constraint). It is not published.
- Planned: per-Harbor tables for the inbox of consumed event ids ([ADR-0004](../adr/0004-consume-kafka-events-in-ship-backend-through-an-idempotent-inbox.md)) and the Known Harbors ([ADR-0005](../adr/0005-discover-harbors-via-harbor-opened-events-on-a-compacted-topic.md)). The Cargo catalog and Catain roster stay identical seeds at every Harbor.

## 8.3 Transactions and event publication

`ShippingManagementService.releaseShipping` (`@Transactional`) commits the shipping state change and
the `shipping_outbox` insert together. `CargoLoadManagementService.addCargo` and `removeCargo` are
transactional too: a load takes one Cargo out of the Stock and an unload of Loaded Cargo puts one back,
in the same transaction as the cargo load, so a rejected load (Max Weight, already loaded, out of
Stock) rolls back and leaves the Stock unchanged. Publication to Kafka is
asynchronous and at-least-once via Debezium; consumers must tolerate duplicates.
See [ADR-0002](../adr/0002-transactional-outbox-via-debezium.md).

Planned ([ADR-0004](../adr/0004-consume-kafka-events-in-ship-backend-through-an-idempotent-inbox.md)):
a second kind of transaction boundary, the handling of a consumed event. The inbox row, the state
change and any outbox row it causes (for example `ship-arrived`) commit together; an event id already
in the inbox is skipped, so redelivery has no effect.

## 8.4 Error handling (current state)

- One global handler, `ProblemDetailsExceptionHandler` (`@RestControllerAdvice` in `driving-adapter`), renders errors as RFC 9457 Problem Details (`application/problem+json`).
- Missing resources: driving adapters map `null` from a port to `404` (`ResponseStatusException`, "Unable to find resource"), rendered as Problem Details.
- Cargo loading rule violations (`ShipTooHeavyException`, `ItemAlreadyLoadedException`, `CargoOutOfStockException`) propagate from the domain and are answered with `409`; `detail` is the exception's message in domain language. A load without `cargoId` is a `400`.
- Other violations (`IllegalArgumentException` / `IllegalStateException`, e.g. a second Shipping, unknown Catain) are not mapped yet and surface as `500`.

## 8.5 Configuration

Spring `application.yml` holds defaults (DB URL, MinIO URL and bucket `catains`); the `local` profile
(`application-local.yml`) targets a locally running stack. On Kubernetes, values come from the
`backend-config` ConfigMap and the credential Secrets as environment variables. Planned: every
instance is configured with its **Harbor Name** ([ADR-0003](../adr/0003-run-each-ship-backend-instance-as-one-harbor.md)),
which also derives its Kafka consumer group, and with the Kafka bootstrap servers.

## 8.6 Logging and observability

Spring Boot Actuator exposes `health` (with liveness/readiness probes) and `info`. No application
logging, metrics or tracing beyond framework defaults. The terminal prints events to stdout.

## 8.7 API conventions

REST/JSON under `/web`, resources nested under the ship (`/web/ships/{id}/cargos`,
`/web/ships/{id}/shippings`). Contract: `ship-backend/openapi.yml`. No versioning.

## 8.8 Test Strategy

> Status: **Proposed — decision pending with the project owner.** Today there are **no automated
> tests** in the repository (no backend test sources, no frontend `*.spec.ts`). The levels below are
> a suggestion fitted to the stack; adopt, change or reject them before the first `implement-story` run.

| Level | Scope | Proposed tooling |
|---|---|---|
| Domain unit | `domain` model and services against fake ports — the invariants (Max Weight, duplicate cargo, Shipping lifecycle, Ship Name rules) | JUnit 5 + Kotest assertions / MockK |
| Adapter | each driven adapter against a real Postgres / MinIO; each controller against a mocked port | Spring Boot slice tests (`@DataJpaTest`, `@WebMvcTest`) + Testcontainers |
| Integration | Release → outbox row → Debezium → Kafka topic | Testcontainers (Postgres, Kafka, Debezium) |
| Frontend | components, NgRx reducers/effects/selectors | Angular test runner (Jasmine/Karma or Jest — TODO) |
| Acceptance | one test per Gherkin scenario of a story, against the backend REST API | TODO: Cucumber-JVM vs. plain JUnit named after the scenarios |

> TODO (owner): coverage expectation; whether a `integration-test` module (already declared in the parent `pom.xml` dependency management) becomes the home of the integration level; which test environment (Compose vs. Testcontainers only).
