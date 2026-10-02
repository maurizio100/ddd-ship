> Status: draft

# 8. Crosscutting Concepts

## 8.1 Domain model and language

The ubiquitous language lives in [`../domain/`](../domain/README.md) and is used verbatim in code,
including the "Catain" spelling. Identity is carried by UUID value objects (`ShipId`, `CargoId`,
`ShippingId`, `CatainId`); persistence entities additionally have a numeric surrogate key.

## 8.2 Persistence

- PostgreSQL via Spring Data JPA in `driven-adapter`; domain objects are mapped to separate `*PersistenceEntity` classes, so JPA never leaks into `domain`.
- The schema is owned by **Flyway** (`application/src/main/resources/db/migration`, `V1__init.sql` …); Hibernate DDL generation is off (`ddl-auto: none`).
- Reference data (Cargo catalog, Catain roster, Shipping Quotes) is seeded by migrations `V2`–`V4`.
- Binary data (Catain Images) lives in MinIO, not in the database.

## 8.3 Transactions and event publication

The only explicit transaction boundary is `ShippingManagementService.releaseShipping` (`@Transactional`):
the shipping state change and the `shipping_outbox` insert commit together. Publication to Kafka is
asynchronous and at-least-once via Debezium; consumers must tolerate duplicates.
See [ADR-0002](../adr/0002-transactional-outbox-via-debezium.md).

## 8.4 Error handling (current state)

- Missing resources: driving adapters map `null` from a port to `404` (`ResponseStatusException`, "Unable to find resource").
- Domain rule violations on cargo loading (`ShipTooHeavyException`, `ItemAlreadyLoadedException`) are caught in the service and the **unchanged ship is returned with 200**; the client cannot tell the load was rejected.
- Other violations (`IllegalArgumentException` / `IllegalStateException`, e.g. a second Shipping, unknown Catain) are not mapped and surface as `500`.
- There is no global exception handler and no error response format.

## 8.5 Configuration

Spring `application.yml` holds defaults (DB URL, MinIO URL and bucket `catains`); the `local` profile
(`application-local.yml`) targets a locally running stack. On Kubernetes, values come from the
`backend-config` ConfigMap and the credential Secrets as environment variables.

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
