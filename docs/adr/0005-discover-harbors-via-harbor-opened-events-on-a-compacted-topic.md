# ADR-0005: Discover Harbors via harbor-opened events on a compacted topic

## Status
Proposed

## Date
2026-10-03

## Context
A Release names a Destination Harbor (ADR-0003), so each Harbor must know which other Harbors
exist. Harbors come and go as deployments, and a newly started Harbor must learn about all Harbors
that started before it.

## Decision
We will have each Harbor write a `harbor-opened` event with its Harbor Name through its outbox on
startup, routed to the topic `hexagonship-harbor`, which is configured with `cleanup.policy=compact`
and keyed by a name-based UUID (UUIDv5) of the Harbor Name. Every Harbor consumes this topic from the
beginning (ADR-0004) into a local list of Known Harbors, which are the choices for a Destination Harbor.

## Consequences

### Positive
- Adding a Harbor needs no change to any other Harbor; it announces itself.
- Compaction keeps the latest event per Harbor, so a new or restarted Harbor reads the complete list.
- It uses only the outbox and the consumer the feature needs anyway; no new component.

### Negative
- The topic has to be created with compaction before the first Harbor starts (manual or init script); the connector cannot set it.
- A Harbor that is shut down for good stays known until something removes it (no `harbor-closed` yet).
- The outbox table is still named `shipping_outbox` while it now also carries Harbor events.

### Neutral
- Writing the event on every startup is harmless: the key is stable and compaction keeps one.

## Alternatives considered
- **Static configuration**: every instance lists all other Harbors. Rejected because each new Harbor needs a redeploy of every other one, and typos send ships nowhere.
- **A central registry service**: Harbors register with it and query it. Rejected because it adds a deployable and a single point of failure, against the few-moving-parts constraint.

## References
- Domain: [Shipping glossary](../domain/contexts/Shipping/glossary.md) (Harbor Opened, Known Harbors, Harbor Name)
- arc42: [03 Context](../arc42/03-context.md), [06 Runtime](../arc42/06-runtime.md), [07 Deployment](../arc42/07-deployment.md), [08 Crosscutting](../arc42/08-crosscutting.md) (8.2, 8.5)
- Config: `kafka-connect/connectors/ship-outbox-connector.json`
- Related: ADR-0002, ADR-0003, ADR-0004
