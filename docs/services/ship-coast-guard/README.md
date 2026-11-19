# ship-coast-guard

The Coast Guard. A standalone Kafka Streams app that reads Shipping Published events from
`hexagonship-shipping`, counts per ship the voyages carrying Contraband, and publishes Ship Imprisoned
(`ship-imprisoned`) to `hexagonship-coast-guard` when a ship carries Contraband three times within 10
minutes ([ADR-0011](../../adr/0011-imprison-contraband-runners-with-a-coast-guard-on-kafka-streams.md)).
It keeps its own copy of the event model and its counts in a Kafka Streams state store; it never calls
ship-backend and has no database. Counting and publishing Ship Imprisoned are built (EPIC-005); the
Harbors' Confiscation is not.

- **Bounded context(s):** CoastGuard
- **Building block:** ship-coast-guard ([05-building-blocks.md](../../arc42/05-building-blocks.md), Level 1)
- **Code lives in:** `ship-coast-guard/`
- **Tech:** Kotlin 2.2 on Java 21, Kafka Streams 4.0, Jackson, Maven
- **Build & test:** see *How to run* below; the tests use `TopologyTestDriver` and need no broker ([08-crosscutting.md](../../arc42/08-crosscutting.md), 8.8)
- **Talks to:** Kafka (reads `hexagonship-shipping`, writes `hexagonship-coast-guard`)

## How to run

From `ship-coast-guard/` (JDK 21 and a local Maven; there is no wrapper). These are the commands the
delivery agents run.

| Task | Command |
|---|---|
| Build and all tests | `mvn -q verify` |
| One test class | `mvn -q test -Dtest=<TestClass>` |
| Lint | none configured |
| Start | `mvn -q compile exec:java` (streams from Kafka at `localhost:9094`, or `KAFKA_BOOTSTRAP_SERVERS`, so start `docker-compose-kafka.yml` first) |

## Documents in this folder

Read this index first and then open **only** the documents your task needs.

| Document | What it contains | Read it when |
| --- | --- | --- |
| `guidelines/` | Testing conventions. `guidelines/README.md` indexes them. | Writing or reviewing code in this component |
| `decisions/` | Component-scoped mini-ADRs — decisions binding only this component. `decisions/README.md` indexes them all. | Planning or reviewing a change: scan the index, open the entries that bear on it, and check the change does not contradict one. |
