# ship-backend

The Hexagonship backend: all domain logic and persistence for managing ships and Catains, loading
Cargo, and preparing and Releasing Shippings. It owns the PostgreSQL schema (through Flyway) and the
Catain Images in MinIO, and on Release it writes the Shipping Published event to the transactional
outbox. A ship Released to this Harbor arrives by itself: on consuming its Shipping Published the
backend takes it into the fleet with its Cargo aboard (an Incoming Ship), and writes Ship Arrived to the outbox.
On the harbor management page the User unloads an Incoming Ship: the backend pays its Delivery Price from
the Savings, puts its Cargo into the Stock, and adds the Delivery Price to the ship's Earnings (or, at the
ship's own Home Harbor, puts it back into the Savings at once); the Earnings travel with the ship in
Shipping Published; when the ship arrives at its Home Harbor, its Earnings go into the Savings. Or the User refuses it: nothing is paid, and the backend
Releases it to its Home Harbor with its Cargo aboard, or, at its own Home Harbor, keeps it in the fleet with
that Cargo aboard to be delivered to another Harbor.
When it consumes Ship Arrived for a ship it Released, it sets that Shipping to `DONE` and the ship
leaves its fleet.
It does not publish to Kafka itself (Debezium does that), and it renders no UI.

- **Bounded context(s):** Fleet, CargoLoading, Shipping, Trade
- **Building block:** ship-backend ([05-building-blocks.md](../../arc42/05-building-blocks.md), Level 1 and Level 2)
- **Code lives in:** `ship-backend/` (Maven modules `domain`, `driving-adapter`, `driven-adapter`, `application`)
- **Tech:** Kotlin 2.2, Java 21, Spring Boot 3.5, Spring Data JPA, PostgreSQL 13 + Flyway, MinIO, spring-kafka
- **Build & test:** see *How to run* below
- **Talks to:** PostgreSQL, MinIO; called by ship-frontend over REST `/web`; its outbox is read by Debezium → Kafka → ship-terminal; consumes other Harbors' events from Kafka (inbox); pushes fleet changes to ship-frontend over SSE (`/web/fleet-events`)

## How to run

From `ship-backend/` (JDK 21; the Maven wrapper needs no local Maven). These are the commands the
delivery agents run; `guidelines/testing.md` says which tests each one covers.

| Task | Command |
|---|---|
| Compile | `./mvnw -q compile` |
| Inner loop (one module and what it needs, no Docker) | `./mvnw -q verify -pl <module> -am` |
| All tests, before pushing (no Docker) | `./mvnw -q verify` |
| One test class | `./mvnw -q test -pl <module> -am -Dtest=<TestClass> -Dsurefire.failIfNoSpecifiedTests=false` |
| Docker tests (Postgres, Kafka) | `./mvnw verify -Pdb`, locally only for persistence, migration or transaction changes; CI always runs it |
| Lint | none configured |
| Start (with Postgres, MinIO and the frontend) | from the repo root: `docker compose -f docker-compose.yml up -d`; with Kafka: `docker compose -f docker-compose-kafka.yml up -d` then `docker compose -f docker-compose-app.yml up -d` |
| Start two Harbors | see [`how-to-run-two-harbors.md`](how-to-run-two-harbors.md) |

## Documents in this folder

Read this index first and then open **only** the documents your task needs.

| Document | What it contains | Read it when |
| --- | --- | --- |
| `guidelines/` | Architecture, naming, persistence, API design and error handling, testing. `guidelines/README.md` indexes them. | Writing or reviewing production or test code in this component |
| `decisions/` | Component-scoped mini-ADRs — decisions binding only this component. `decisions/README.md` indexes them all. | Planning or reviewing a change: scan the index, open the entries that bear on it, and check the change does not contradict one. |
| `how-to-run-two-harbors.md` | Start and stop two Harbors (Tortuga, Port Royal) against one Kafka: Compose projects, topic, connectors, ports. | Running or verifying several Harbors locally |
