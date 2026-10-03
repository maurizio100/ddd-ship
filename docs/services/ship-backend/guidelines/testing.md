# ship-backend — testing

**Run:** `cd ship-backend && ./mvnw verify`

Test dependencies (JUnit 5, Kotest assertions, MockK, springmockk, Testcontainers, `spring-boot-starter-test`)
are managed in the parent `pom.xml` and declared with `test` scope in the module that uses them.

## Levels

| Level | Lives in | Scope | Tooling |
|---|---|---|---|
| Domain unit | `domain/src/test/kotlin` | model invariants and domain services against MockK'd driven ports | JUnit 5, Kotest assertions, MockK; no Spring |
| Driving adapter | `driving-adapter/src/test/kotlin` | one controller against a mocked driving port: status, JSON shape, Problem Details | `@WebMvcTest` + `@MockkBean` (springmockk) |
| Driven adapter | `driven-adapter/src/test/kotlin` | one port adapter against real Postgres (Flyway schema) | `@DataJpaTest` + Testcontainers PostgreSQL |
| Acceptance | `application/src/test/kotlin/com/sonicdevelopment/application/acceptance` | each Gherkin scenario of a story, over HTTP against the running app | `@SpringBootTest(webEnvironment = RANDOM_PORT)` + Testcontainers PostgreSQL (+ MinIO when needed) |
| Integration (outbox → Kafka) | the `integration-test` module | Release → outbox row → Debezium → topic | Testcontainers Postgres, Kafka, Debezium |

Every behavioural story has acceptance tests, and every rule it adds or changes has a domain unit
test. Adapter tests are written when the story touches that adapter.

## Acceptance tests

- One test class per feature, `<Feature>AcceptanceTest`, e.g. `CargoLoadingAcceptanceTest`.
- One test method per Gherkin scenario, named with the scenario title in backticks:
  ``fun `a ship cannot be loaded beyond its Max Weight`()``. A scenario outline gets a
  `@ParameterizedTest` with one row per example.
- The test body follows the scenario's Given / When / Then, marked with `// Given`,
  `// When` and `// Then` comments.
- There are no `.feature` files and no Cucumber; the story in the tracker is the spec.

## Fixtures

- Test data builders are top-level functions named after the domain term with an article:
  `aShip()`, `aCargo(weight = 5.0)`, `aShipping()`, `aCatain()`. Every parameter has a default,
  and the test passes only what it cares about.
- Builders live in a `fixtures` package in the test sources of the module that uses them
  (`domain/src/test/kotlin/.../domain/fixtures`, `application/.../acceptance/fixtures`).
- Catains, Cargo and Shipping Quotes come from the Flyway seed data. Use the seeded rows; don't
  insert copies.

## Isolation

- One Postgres Testcontainer per JVM (singleton, reused across test classes), migrated by Flyway.
  Driven-adapter tests load the migrations from the `application` module with
  `spring.flyway.locations: filesystem:../application/src/main/resources/db/migration` and use
  `@AutoConfigureTestDatabase(replace = NONE)`, since H2 is on the compile classpath.
- Application tests run with an unreachable Kafka (`spring.kafka.bootstrap-servers=localhost:1`) and
  a `harbor.name`, unless they test messaging.
- Before each acceptance or driven-adapter test, truncate the mutable tables (`ships_cargos`,
  `shippings`, `ships`, `shipping_outbox`, `inbox_events`). Reference tables (`cargos`, `catains`, `quotes`) are left
  intact.
- Tests don't depend on the wall clock. Code that reads the time (the Sailors Code uses the current
  minute) takes a `java.time.Clock`, and tests pass a fixed one.
