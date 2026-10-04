# ADR-0006: Push fleet changes to the frontend with Server-Sent Events

## Status
Accepted

## Date
2026-10-04

## Context
Since Harbors exchange ships (ADR-0003, ADR-0004), a Harbor's fleet changes without any action of its
own User: a ship arrives when the Harbor consumes its `shipping-published`, and a ship leaves when it
consumes `ship-arrived`. ship-frontend only fetched the Available Ships on demand, so the User saw these
changes only after a reload. The changes are rare, flow only from server to browser, and must never
show something that was rolled back. This is the first server push in Hexagonship.

## Decision
We will push fleet changes from ship-backend to ship-frontend with **Server-Sent Events**:

- `GET /web/fleet-events` is a `text/event-stream` with the events `ship-arrived` and `ship-left`. The
  backend keeps the open streams in an in-memory emitter registry and sends a heartbeat comment every 15 s.
- The domain announces a change through a new driven port, `FleetEventsPort`. Its adapter registers a
  `TransactionSynchronization` and pushes in `afterCommit`, so nothing is pushed on rollback. Without an
  active transaction it pushes at once.
- That adapter lives in `driving-adapter`, next to the controller that owns the HTTP connections: it is
  the first driven port implemented in `driving-adapter`, because only that module has Spring MVC, and
  adapters may not depend on each other (ADR-0001).
- The frontend opens an `EventSource` through an NgRx effect. An arrival triggers a refetch of the fleet
  and a short notice; a departure removes the ship from the store. Proxies (Compose nginx, k8s Ingress)
  get an unbuffered, long-timeout route for this one path.

## Consequences

### Positive
- The fleet updates within moments of the commit, without polling traffic.
- Plain HTTP: the browser's `EventSource` reconnects by itself, and nginx/Ingress need only buffering
  and timeout settings, no protocol upgrade.
- The domain stays free of web and transaction APIs; it gains one driven port.

### Negative
- The emitter registry is per instance. With several replicas of one Harbor (k8s runs 2), a tab only
  sees events its own replica handled (arc42 R-11).
- Each open tab holds one long-lived HTTP/1.1 connection, which counts against the browser's limit of
  about six connections per origin.
- Events are not persisted or replayed (`Last-Event-ID`): a tab that was disconnected catches up only
  with the next event's refetch, or a reload.

- Sends to the open streams are blocking servlet writes. They run on one sender thread owned by
  `FleetEventEmitters`, which keeps their order and keeps a stalled browser from stalling the Kafka listener
  that handled the Arrival; an event that waits behind a stalled stream is delayed for the other tabs too.

### Neutral
- These fleet events are not domain events and not outbox events; they never leave the Harbor.

## Alternatives considered
- **Polling `/web/ships`**: simplest, but trades a delay against constant requests, for changes that are rare.
- **WebSocket / STOMP**: bidirectional, which nothing here needs; it adds a broker abstraction, a client
  library and an upgrade-capable proxy route.
- **`@TransactionalEventListener`**: needs the domain to publish Spring `ApplicationEvent`s, and the
  backend has no internal application events (CLAUDE.md). The adapter defers to commit itself instead.
- **The push adapter in `driven-adapter`**: that module has no web stack, and it would have to reach
  the emitters owned by the controller in `driving-adapter`, which the dependency rule forbids.

## References
- Domain: [Shipping glossary](../domain/contexts/Shipping/glossary.md) (Arrival, Ship Arrived), [Fleet glossary](../domain/contexts/Fleet/glossary.md) (Available Ships)
- arc42: [05 Building Blocks](../arc42/05-building-blocks.md), [06 Runtime](../arc42/06-runtime.md) (6.6), [07 Deployment](../arc42/07-deployment.md), [08 Crosscutting](../arc42/08-crosscutting.md) (8.3, 8.7), [11 Risks](../arc42/11-risks.md) (R-11)
- Contract: `ship-backend/openapi.yml` (`/web/fleet-events`)
- Config: `ship-frontend/nginx/default.conf.template`, `k8s/50-ingress.yaml`
- Related: ADR-0001, ADR-0003, ADR-0004
