# ADR-0002: Publish domain events via a transactional outbox and Debezium

## Status
Proposed

## Date
2026-10-02

## Context
When a ship is Released, other systems (the harbor terminal) must learn about it. Writing to the
database and publishing to Kafka in the same request is a dual write: either can fail after the other
has succeeded, losing or inventing events. Exploring the Transactional Outbox with Kafka Connect /
Debezium is an explicit goal of the project (README). *Recorded retroactively; rationale inferred from
the README and the code.*

## Decision
We will publish domain events by inserting them into the `shipping_outbox` table in the same database
transaction as the state change, and let Debezium (Kafka Connect, Outbox EventRouter) stream that table
to Kafka, routed by `aggregate_type` to `hexagonship-<aggregate_type>` (today `hexagonship-shipping`).

## Consequences

### Positive
- A Release and its `shipping-published` event commit or roll back together; no event is lost or published for a failed Release.
- The backend has no Kafka client dependency; publication is infrastructure.
- Kafka being down does not block Releases; events are delivered once Connect catches up.

### Negative
- Delivery is at-least-once; consumers must tolerate duplicates.
- Requires PostgreSQL logical replication, Kafka, Zookeeper and Kafka Connect, plus hand-registered connectors — heavy infrastructure for one event, and not yet present in the Kubernetes deployment.
- Publication is asynchronous; there is a lag between Release and the event.
- Outbox rows are never cleaned up by the application.

### Neutral
- The event payload (`ShippingEvent`) is a JSON document owned by the backend; the terminal keeps its own copy of the shape.

## Alternatives considered
- **Publish to Kafka directly from the service (dual write).** Rejected because a failure between the DB commit and the publish loses or duplicates events.
- **Outbox with an in-application polling publisher.** Rejected because it adds scheduling and Kafka code to the backend, and the project's aim is to try out Debezium CDC.
- **No events; the terminal polls the REST API.** Rejected because it couples the terminal to the backend's API and availability.

## References
- arc42: [04 Solution Strategy](../arc42/04-solution-strategy.md), [06 Runtime — 6.3 Release](../arc42/06-runtime.md), [03 Context](../arc42/03-context.md), [08 Crosscutting — 8.3](../arc42/08-crosscutting.md)
- Config: `kafka-connect/connectors/ship-outbox-connector.json`
