# Hexagonship

Pet project for trying out Hexagonal Architecture, Domain-Driven Design and the Transactional Outbox pattern with Kafka Connect/Debezium

## Where things live

- `docs/domain/` — bounded contexts, glossary, context map
- `docs/arc42/` — architecture documentation
- `docs/adr/` — architecture decision records

## Domain at a glance

Hexagonship is a playful harbor: a **User** registers ships, each commanded by a cat **Catain**, loads
them with **Cargo** up to a Max Weight of 15.0, and **Releases** them on a **Shipping**; each departure
is published and announced on a harbor terminal. Bounded contexts: **Fleet**, **CargoLoading**,
**Shipping**, **HarborTerminal**. Core flow: create ship (Fleet) → new Shipping → load Cargo
(CargoLoading) → Release → Sailors Code + Shipping Quote → `shipping-published` via outbox/Kafka →
HarborTerminal. Full model (derived from code, candidates for review): `docs/domain/`.

## Conventions

- **Every source code change must be motivated by a story.** Use `gherkin-story-authoring` for
  behavioral changes (new features, rule changes) and `chore-story` for non-behavioral changes
  (layout tweaks, refactorings, dependency updates, tooling).
- Story IDs: `STORY-NNN` / Epic IDs: `EPIC-NNN` / ADR IDs: `NNNN`
- Branch names: `STORY-NNN-kebab-case-title`
- Commit messages: Conventional Commits with `Refs: STORY-NNN` in footer
- One change request per story, draft while in progress, squash on merge
- Default branch: `main`
