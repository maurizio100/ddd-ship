# ship-backend — testing

**Run:** `cd ship-backend && ./mvnw verify` (no Docker needed), and
`./mvnw verify -Pdb` for the whole suite including the tests that need Docker. Local runs need JDK 21
(`JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64`); the shell default may be older.

Test dependencies (JUnit 5, Kotest assertions, MockK, springmockk, Testcontainers, `spring-boot-starter-test`)
are managed in the parent `pom.xml` and declared with `test` scope in the module that uses them.

## Levels

| Level | Lives in | Scope | Tooling |
|---|---|---|---|
| Domain unit | `domain/src/test/kotlin` | model invariants and domain services against MockK'd driven ports | JUnit 5, Kotest assertions, MockK; no Spring |
| Driving adapter | `driving-adapter/src/test/kotlin` | one controller against a mocked driving port: status, JSON shape, Problem Details | `@WebMvcTest` + `@MockkBean` (springmockk) |
| Driven adapter | `driven-adapter/src/test/kotlin` | one port adapter against real Postgres (Flyway schema) | `@DataJpaTest` + Testcontainers PostgreSQL |
| Acceptance | `application/src/test/kotlin/com/sonicdevelopment/application/acceptance` | each Gherkin scenario of a story, over HTTP against the running app | `@SpringBootTest(webEnvironment = RANDOM_PORT)` + Testcontainers PostgreSQL (+ MinIO when needed) |

Every behavioural story has acceptance tests, and every rule it adds or changes has a domain unit
test. Adapter tests are written when the story touches that adapter.

## The `db` profile

Any test that needs Docker (a Testcontainer) must carry `@DbTest`, the meta-annotation for
`@Tag("db")` that exists in the test sources of `driven-adapter` and `application`. The parent `pom.xml`
makes surefire exclude the `db` group by default; `-Pdb` lifts the exclusion. A test that starts a
container without the tag breaks the Docker-free default build.

Tagged today: every `driven-adapter` repository adapter test, the Kafka-based `application` acceptance tests,
`ShipBackendStartupTest`, `ShipsCargosPrimaryKeyMigrationTest`, `FleetEventsAfterCommitIntegrationTest`
and `ReferenceDataIdsTest`. Tagging an outer class covers its `@Nested` classes. CI
(`.github/workflows/test.yml`) runs `./mvnw verify -Pdb` on every pull request and push to `main`.

## Fakes

- Acceptance tests that need no Kafka use `@FakeHarborTest` and the in-memory fakes in
  `application/.../acceptance/fixtures`, and carry no `@DbTest`.
- Fakes hold plain records and rebuild fresh domain objects on every read, never live ones.
- They mirror the adapter's `ON CONFLICT` semantics (first write wins; `putIntoStock` adds).
- They do not roll back, so a rollback guarantee belongs in a `-Pdb` test.
- Reset them in `@BeforeEach` (`FakeDrivenPorts.reset()`).

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
- Messaging acceptance tests import the singleton `KafkaTestcontainer` (`apache/kafka`, one per JVM,
  topics created up front) when a scenario is about consumer-group or read-from-the-beginning
  behaviour a mocked listener cannot show. Another Harbor's event is simulated by producing the
  record Debezium would relay (`id` and `eventType` headers, payload as a JSON string literal), and
  "processed" is awaited with Awaitility on the event id in `inbox_events`. Each Harbor is its own
  `@Nested` class with its own `harbor.name` and `@DirtiesContext(AFTER_CLASS)`, so two Harbor
  listeners never run at the same time. To observe a Harbor that opens later, set
  `spring.kafka.listener.auto-startup=false` and start its container through
  `KafkaListenerEndpointRegistry`.
- Before each acceptance or driven-adapter test, truncate the mutable tables (`ships_cargos`,
  `shippings`, `ships`, `shipping_outbox`, `inbox_events`, `known_harbors`). Reference tables (`cargos`, `catains`, `quotes`) are left
  intact. `stocks` is not truncated but reset to the Starting Stock (`resetStockToStartingStock()`),
  since an empty Stock would make every Cargo unavailable.
- A scenario about a Harbor opening for the first time creates a fresh database in the shared
  Postgres container and runs Flyway against it, rather than relying on the already-migrated one.
- Tests don't depend on the wall clock. Code that reads the time (the Sailors Code uses the current
  minute) takes a `java.time.Clock`, and tests pass a fixed one.
