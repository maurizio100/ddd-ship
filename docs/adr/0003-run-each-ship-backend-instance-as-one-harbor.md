# ADR-0003: Run each ship-backend instance as one Harbor

## Status
Proposed

## Date
2026-10-03

## Context
Ships should sail from one Harbor to another, each Harbor having its own fleet and Stock of Cargo
(see the "Planned" sections in `docs/domain`). Today Hexagonship is a single backend with one database
and one implicit harbor, and nothing ever ends a Shipping (R-1). Exploring event-driven integration
over Kafka and Debezium is a goal of the project, so the Harbors should be independent systems that
only talk through events. The project constraint is a single developer who wants few moving parts.

## Decision
We will run each Harbor as a complete ship-backend deployment with its own PostgreSQL database,
outbox and Debezium connector, identified by a configured Harbor Name. Ships move between Harbors
only through events; a ship keeps its existing UUID Ship Id (`ships.ship_id`) at every Harbor, and the
database surrogate key stays local to one Harbor.

## Consequences

### Positive
- Every Harbor is the same artifact; a new Harbor is a new deployment, not new code.
- Harbors are truly autonomous: one Harbor being down does not stop another from loading or Releasing.
- The internal model (shared `Ship`, ADR-0001) is unchanged; no Harbor column runs through every query.

### Negative
- Each Harbor needs its own database, connector (distinct replication slot and connector name) and frontend; local setups grow heavier.
- Ship, Catain and Cargo data are copied between Harbors through events; the Catain roster and Cargo catalog must be seeded identically everywhere (today guaranteed by the Flyway seeds).
- Cross-Harbor consistency is eventual: a ship is briefly at sea in neither fleet.

### Neutral
- A Harbor Name becomes required configuration (`application.yml` / k8s ConfigMap).
- Inbound events and Harbor discovery are decided separately (ADR-0004, ADR-0005).

## Alternatives considered
- **A separate Harbor service next to one ship-backend**: a new deployable owning Stock and Arrival. Rejected because it splits Stock away from CargoLoading, which already loads from it, and adds a second codebase for the same ships.
- **One backend hosting many Harbors (a Harbor column)**: multi-tenant data in one database. Rejected because it touches every table and query, and Harbors would talk in-process, leaving nothing for Kafka to carry.

## References
- Domain: [glossary](../domain/glossary.md) (Harbor, Ship Id), [context map](../domain/context-map.md) (row 5)
- arc42: [03 Context](../arc42/03-context.md), [05 Building Blocks](../arc42/05-building-blocks.md), [07 Deployment](../arc42/07-deployment.md), [08 Crosscutting](../arc42/08-crosscutting.md) (8.1, 8.5), [11 Risks](../arc42/11-risks.md) (R-1, R-4)
- Related: ADR-0001, ADR-0002, ADR-0004, ADR-0005
