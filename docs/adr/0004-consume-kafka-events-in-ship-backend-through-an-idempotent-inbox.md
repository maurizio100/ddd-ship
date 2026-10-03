# ADR-0004: Consume Kafka events in ship-backend through an idempotent inbox

## Status
Proposed

## Date
2026-10-03

## Context
With several Harbors (ADR-0003), a Harbor must react to other Harbors' events: Shipping Published
addressed to it (Arrival), Ship Arrived (end the Shipping) and Harbor Opened (ADR-0005). ADR-0002
gave the backend an outbound path only and listed "no Kafka client dependency" as a benefit. Delivery
is at-least-once, and Arrival puts Cargo into Stock, an increment that must not be repeated for a
redelivered event.

## Decision
We will add a Kafka consumer to ship-backend as a driving adapter (spring-kafka) that calls the
existing driving ports, with one consumer group per Harbor (derived from the Harbor Name). Each
consumed event's id is written to an inbox table in the same transaction as the state change it
causes; an event whose id is already in the inbox is skipped. Outbound publication stays as in
ADR-0002.

## Consequences

### Positive
- A redelivered event never unloads Cargo twice or ends a Shipping twice.
- The inbox row and the state change commit together, mirroring the outbox on the way out.
- The consumer is just another driving adapter; the domain stays free of Kafka.

### Negative
- ship-backend now depends on spring-kafka and on a reachable broker; this overturns ADR-0002's "no Kafka client" benefit and makes R-4 (no Kafka on k8s) blocking for Harbor voyages.
- The backend keeps its own inbound copy of each event shape (no shared classes), adding to R-8.
- The inbox table grows and needs a cleanup policy eventually.
- Every Harbor reads every event and filters by Destination/Origin Harbor; acceptable for a handful of Harbors.

### Neutral
- Replicas of one Harbor share its consumer group, so each event is handled once per Harbor.
- The backend must start (listeners may stay idle) when Kafka is unavailable, so the app-only Compose setup keeps working.

## Alternatives considered
- **Business-key idempotency only**: rely on Ship Id and Shipping id being unique. Rejected because adding Cargo to Stock is not naturally idempotent.
- **A separate consumer service calling the REST API**: keeps the backend Kafka-free. Rejected because it adds a deployable and cannot make "seen this event" and the state change atomic.
- **Keep the backend Kafka-free**: Rejected because Harbors could then not react to each other at all.

## References
- Domain: [Shipping](../domain/contexts/Shipping/Shipping.md) (Planned), [CargoLoading glossary](../domain/contexts/CargoLoading/glossary.md) (Unloading on Arrival)
- arc42: [03 Context](../arc42/03-context.md), [05 Building Blocks](../arc42/05-building-blocks.md), [06 Runtime](../arc42/06-runtime.md), [08 Crosscutting](../arc42/08-crosscutting.md) (8.2, 8.3), [11 Risks](../arc42/11-risks.md) (R-4, R-8)
- Related: ADR-0001, ADR-0002 (extended, not superseded), ADR-0003, ADR-0005
