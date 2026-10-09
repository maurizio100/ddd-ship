# ship-backend — testing

**Run locally** ([decision 0001](../decisions/0001-run-only-the-default-build-locally.md)), on JDK 21
(`JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64`; the shell default may be older):

- while working: `cd ship-backend && ./mvnw -q verify -pl <module> -am` (no Docker needed);
- once before pushing: `./mvnw -q verify`;
- `-Pdb` (the tests that need Docker) only when the change touches a persistence adapter, a Flyway
  migration or a transaction boundary, and only for that module:
  `./mvnw -q verify -Pdb -pl driven-adapter -am` or `-pl application -am`. CI runs the full
  `./mvnw verify -Pdb` on every pull request.

Test dependencies (JUnit 5, Kotest assertions, MockK, springmockk, Testcontainers, `spring-boot-starter-test`)
are managed in the parent `pom.xml` and declared with `test` scope in the module that uses them.

## Levels

| Level | Lives in | Scope | Tooling | Runs in |
|---|---|---|---|---|
| Domain unit | `domain/src/test/kotlin` | model invariants and domain services against MockK'd driven ports | JUnit 5, Kotest assertions, MockK; no Spring | default build |
| Driving adapter | `driving-adapter/src/test/kotlin` | one controller against a mocked driving port (status, JSON shape, Problem Details); one Kafka listener against a mocked driving port | controllers: `@WebMvcTest` + `@MockkBean` (springmockk); listeners: plain JUnit + MockK | default build |
| Acceptance | `application/src/test/kotlin/com/sonicdevelopment/application/acceptance` | each Gherkin scenario of a story, over HTTP against the real Spring wiring with in-memory fakes in place of the driven ports | `@FakeHarborTest` (`@SpringBootTest(RANDOM_PORT)`) | default build |
| Postgres | `driven-adapter/src/test/kotlin` and `application/src/test/kotlin/com/sonicdevelopment/application` | adapter tests (real SQL, Flyway schema); migration and startup tests; transaction tests | adapters: `@DataJpaTest`; the rest: `@SpringBootTest` + `PostgresTestcontainer` | `-Pdb` |

Every behavioural story has acceptance tests, every rule it adds or changes has a domain unit test, and an
adapter it touches gets an adapter test (arc42 8.8).

## The `db` profile

Any test that starts a container must carry `@DbTest`, the meta-annotation for `@Tag("db")` that exists in
the test sources of `driven-adapter` and `application`. The parent `pom.xml` makes surefire exclude the
`db` group by default; `-Pdb` lifts the exclusion. A test that starts a container without the tag breaks
the Docker-free default build.

Use real Postgres only when the guarantee needs it: native `ON CONFLICT` SQL, a Flyway migration, or
transaction atomicity and rollback. Everything else runs against the fakes.

Tagged today: every `driven-adapter` repository adapter test, `ShipBackendStartupTest`,
`ShipsCargosPrimaryKeyMigrationTest`, `FleetEventsAfterCommitIntegrationTest`, `ReferenceDataIdsTest` and
`FailedChangeLeavesNoTraceTest`. Tagging an outer class covers its `@Nested` classes. CI
(`.github/workflows/test.yml`) runs `./mvnw verify -Pdb` on every pull request and push to `main`.

## Transaction tests

- A state change that writes an outbox or inbox row gets a `-Pdb` test (in `application`, a `@SpringBootTest`
  with `PostgresTestcontainer`) that forces a later write in the same transaction to fail, then asserts that
  no state change, inbox row, Arrival row or outbox row remains. The fakes cannot roll back, so no acceptance
  test can show this (ADR-0002, ADR-0004).
- Call the driving port (the transaction boundary), not the REST layer, so the test does not depend on
  how an unexpected exception is mapped to a response.
- Force the failure with a `@Primary` decorator bean in a nested `@TestConfiguration`: it delegates to the
  real adapter (`Port by real`) and throws a `RuntimeException` once armed. Do not use a spy; the adapters are
  proxied. Disarm the decorator in `@BeforeEach`.
- The failure must be a `RuntimeException`: the services use `jakarta.transaction.Transactional`, which does
  not roll back on checked exceptions.
- Let a write succeed before the failing one, and assert that it did (a counter on the decorator), so the
  test proves the rollback of a write that really happened.
- `FailedChangeLeavesNoTraceTest` is the reference.

## Fakes

- Acceptance tests use `@FakeHarborTest` and the in-memory fakes in
  `application/.../acceptance/fixtures`, and carry no `@DbTest`.
- Fakes hold plain records and rebuild fresh domain objects on every read, never live ones.
- They mirror the adapter's `ON CONFLICT` semantics (first write wins; `putIntoStock` adds).
- They do not roll back, so a rollback guarantee belongs in a transaction test (above).
- Reset them in `@BeforeEach` (`FakeDrivenPorts.reset()`).
- A new driven port gets an `InMemory...` class implementing it in `acceptance/fixtures`, added to
  `FakeDrivenPorts` and its `reset()`, exposed as a `@Bean` in `FakeDrivenPortsConfiguration`, and given a
  case in `FakeDrivenPortsTest` where it mirrors the adapter's semantics. A new `driven-adapter` bean must be
  kept out of the acceptance context by `DrivenAdapterExcludeFilter`; check that it is before relying on it.

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
- Catains, Cargo and Shipping Quotes are the seeded rows. Acceptance tests read them from `SeedData`
  (the fakes' copy of the seed, kept equal to the Flyway data by `ReferenceDataIdsTest`); don't insert copies.

## Isolation

- **Acceptance tests:** call `FakeDrivenPorts.reset()` in `@BeforeEach`. Another Harbor's event is delivered by
  calling the `ShippingEventListener` / `HarborEventListener` bean with the record Debezium would relay (`id`
  and `eventType` headers, payload as a JSON string literal), built by the `fixtures` record builders. Delivery
  is synchronous, so nothing is awaited. A scenario set at another Harbor is a `@Nested` class with its own
  `harbor.name`, without `@DirtiesContext`.
- **`-Pdb` application tests:** one Postgres Testcontainer per JVM (the singleton `PostgresTestcontainer`),
  migrated by Flyway. Before each test, `truncateMutableTables()` (`ships_cargos`, `shippings`, `ships`,
  `shipping_outbox`, `inbox_events`, `known_harbors`, `arrivals`; the reference tables `cargos`, `catains` and
  `quotes` stay) and `resetStockToStartingStock()` (`stocks` is reset to the Starting Stock, not truncated,
  since an empty Stock would make every Cargo unavailable). Kafka is unreachable
  (`spring.kafka.bootstrap-servers=localhost:1`), `spring.kafka.listener.auto-startup=false`, and a
  `harbor.name` is set.
- **Driven-adapter tests:** the same singleton container, with the migrations loaded from the `application`
  module (`spring.flyway.locations: filesystem:../application/src/main/resources/db/migration`) and
  `@AutoConfigureTestDatabase(replace = NONE)`, since H2 is on the compile classpath. Each test truncates the
  tables it touches.
- Tests don't depend on the wall clock. Code that reads the time (the Sailors Code uses the current
  minute) takes a `java.time.Clock`, and tests pass a fixed one.
