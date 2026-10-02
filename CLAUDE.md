# Hexagonship

Pet project for trying out Hexagonal Architecture, Domain-Driven Design and the Transactional Outbox pattern with Kafka Connect/Debezium

## Tech stack

- **ship-backend** — Kotlin 2.2 on Java 21, Spring Boot 3.5, Maven multi-module (hexagonal: `domain`,
  `driving-adapter`, `driven-adapter`, `application`); Spring Data JPA
- **ship-frontend** — Angular 20 with NgRx, served by nginx; Karma + Jasmine for tests
- **ship-terminal** — standalone Kotlin Kafka consumer (Maven)
- **Data** — PostgreSQL 13 (schema owned by Flyway), MinIO for Catain Images
- **Messaging** — Transactional outbox → Kafka Connect / Debezium 3.0 → Kafka
- **Runtime** — Docker Compose locally, Kubernetes manifests in `k8s/`; images built by GitHub Actions

## How to run things

```
cd ship-backend  && ./mvnw verify      # backend build + tests
cd ship-frontend && npm test           # frontend tests (Karma/Jasmine)
cd ship-frontend && npm start          # frontend dev server
cd ship-terminal && mvn package        # terminal build
docker compose -f docker-compose.yml up -d                # app only (frontend, backend, Postgres, MinIO)
docker compose -f docker-compose-kafka.yml up -d          # Kafka + Kafka Connect
docker compose -f docker-compose-app.yml up -d            # app wired to the Kafka stack
```

## Domain at a glance

Hexagonship is a playful harbor: a **User** registers ships, each commanded by a cat **Catain**, loads
them with **Cargo** up to a Max Weight of 15.0, and **Releases** them on a **Shipping**; each departure
is published and announced on a harbor terminal. Bounded contexts: **Fleet**, **CargoLoading**,
**Shipping**, **HarborTerminal**. Core flow: create ship (Fleet) → new Shipping → load Cargo
(CargoLoading) → Release → Sailors Code + Shipping Quote → `shipping-published` via outbox/Kafka →
HarborTerminal. Full model (derived from code, candidates for review): `docs/domain/`.

## Where things live

- `docs/domain/` — bounded contexts, glossary, context map
- `docs/arc42/` — architecture documentation
- `docs/adr/` — architecture decision records
- `docs/services/<component>/` — per-component docs; start at its `README.md` (purpose + index
  of that component's documents)

## Code structure

- The backend is split **by layer first**: the hexagonal Maven modules are the top-level boundary
  ([ADR-0001](docs/adr/0001-hexagonal-architecture-with-maven-modules.md)). Inside a module, packages
  are split by domain concept (`ship`, `cargo`, `catain`, `shipping`), not by bounded context.
  Fleet, CargoLoading and Shipping share the `Ship` model (Shared Kernel). Moving to a split by
  bounded context needs its own story and ADR.
- The ubiquitous language from `docs/domain/` is used verbatim in code, UI and events, including the
  spelling "Catain".

## Cross-context communication

- **Inside the backend** (Fleet, CargoLoading, Shipping): in-process calls through the shared `Ship`
  model. There are no internal application events.
- **Leaving the backend**: only through the transactional outbox
  ([ADR-0002](docs/adr/0002-transactional-outbox-via-debezium.md)). The state change and the
  outbox row are written in one transaction, and Debezium routes the row to Kafka.
  - `event_type`: kebab-case, past tense (`shipping-published`)
  - topic: `hexagonship-<aggregate_type>` (`hexagonship-shipping`)
  - payload class: `<Name>Event` in `driven-adapter/.../persistence/outbox/events`
- **Consumers** (e.g. ship-terminal) receive events at least once and must tolerate duplicates.
  They keep their own copy of the event model and ignore unknown fields. Never share the backend's
  classes with them.

## Foreign systems

All self-hosted and started by Docker Compose / `k8s/`. Details are in `docs/arc42/03-context.md`.
- PostgreSQL 13: all domain data and the `shipping_outbox` table. Logical replication must be on for Debezium.
- MinIO: Catain Images in bucket `catains`.
- Kafka + Kafka Connect (Debezium): outbox CDC. Compose only, not in `k8s/`. Connectors are registered by hand from `kafka-connect/connectors/`.

## Per-component conventions

Each component's docs live in its own folder, fronted by a `README.md` that states the component's
purpose and indexes the folder's documents. Before working on a story, read the `README.md` of the
component(s) it touches and open only the documents its index points you at:
- `docs/services/ship-backend/README.md` — REST API, domain logic, persistence and outbox for Fleet, CargoLoading and Shipping
- `docs/services/ship-frontend/README.md` — Angular/NgRx UI for managing ships, loading Cargo and Releasing
- `docs/services/ship-terminal/README.md` — Kafka consumer that announces departed ships (HarborTerminal)

## Conventions

- **Every source code change must be motivated by a story.** Use `gherkin-story-authoring` for
  behavioral changes (new features, rule changes) and `chore-story` for non-behavioral changes
  (layout tweaks, refactorings, dependency updates, tooling).
- Story IDs: `STORY-NNN` / Epic IDs: `EPIC-NNN` / ADR IDs: `NNNN`
- Branch names: `STORY-NNN-kebab-case-title`
- Commit messages: Conventional Commits with `Refs: STORY-NNN` in footer
- One change request per story, draft while in progress, squash on merge
- Default branch: `main`
