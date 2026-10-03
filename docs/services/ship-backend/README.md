# ship-backend

The Hexagonship backend: all domain logic and persistence for managing ships and Catains, loading
Cargo, and preparing and Releasing Shippings. It owns the PostgreSQL schema (through Flyway) and the
Catain Images in MinIO, and on Release it writes the Shipping Published event to the transactional
outbox. A ship Released to this Harbor arrives by itself: on consuming its Shipping Published the
backend unloads its Cargo into the Stock, takes it into the fleet and writes Ship Arrived to the outbox.
When it consumes Ship Arrived for a ship it Released, it sets that Shipping to `DONE` and the ship
leaves its fleet.
It does not publish to Kafka itself (Debezium does that), and it renders no UI.

- **Bounded context(s):** Fleet, CargoLoading, Shipping
- **Building block:** ship-backend ([05-building-blocks.md](../../arc42/05-building-blocks.md), Level 1 and Level 2)
- **Code lives in:** `ship-backend/` (Maven modules `domain`, `driving-adapter`, `driven-adapter`, `application`)
- **Tech:** Kotlin 2.2, Java 21, Spring Boot 3.5, Spring Data JPA, PostgreSQL 13 + Flyway, MinIO, spring-kafka
- **Build & test:** `cd ship-backend && ./mvnw verify`
- **Talks to:** PostgreSQL, MinIO; called by ship-frontend over REST `/web`; its outbox is read by Debezium → Kafka → ship-terminal; consumes other Harbors' events from Kafka (inbox)

## Documents in this folder

Read this index first and then open **only** the documents your task needs.

| Document | What it contains | Read it when |
| --- | --- | --- |
| `guidelines/` | Architecture, naming, persistence, API design and error handling, testing. `guidelines/README.md` indexes them. | Writing or reviewing production or test code in this component |
| `decisions/` | Component-scoped mini-ADRs — decisions binding only this component. `decisions/README.md` indexes them all. | Planning or reviewing a change: scan the index, open the entries that bear on it, and check the change does not contradict one. |
| `how-to-run-two-harbors.md` | Start and stop two Harbors (Tortuga, Port Royal) against one Kafka: Compose projects, topic, connectors, ports. | Running or verifying several Harbors locally |
