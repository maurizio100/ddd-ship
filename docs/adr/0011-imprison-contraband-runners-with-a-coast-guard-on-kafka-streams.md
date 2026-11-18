# ADR-0011: Imprison Contraband runners with a Coast Guard built on Kafka Streams

## Status
Accepted

## Date
2026-11-18

## Context
Ships should be imprisoned when they carry Contraband (Ale, Rum, Wine, Tobacco) too often: the third
voyage carrying Contraband within 10 minutes imprisons a ship, its Home Harbor learns of it and frees
it by paying a 50.00 $ Fine (CoastGuard context). Judging "too often" needs every voyage of a ship,
from every Harbor it sails from, but each Harbor keeps only the Shippings it saw itself, and
ADR-0003, ADR-0008 and ADR-0009 rejected a shared registry across Harbors. Every Release already
reaches `hexagonship-shipping` as Shipping Published (ADR-0002), and a Destination Harbor handles the
Arrival as soon as it consumes it (ADR-0004), so a ship is at sea only for the consumer lag. The
project is also a place to try Kafka technology, and Kafka Streams is not used yet: ship-terminal
reads with the plain consumer.

## Decision
We will add a component, `ship-coast-guard`, built with Kafka Streams, that reads
`hexagonship-shipping` beside the Harbors, counts each ship's voyages carrying Contraband by Ship Id
and Shipping id in its own state store, and publishes `ship-imprisoned` to its own topic
`hexagonship-coast-guard` itself, without an outbox. Each Harbor consumes that event through its
idempotent inbox; the Harbor the ship arrived at confiscates its Cargo and, away from the Home
Harbor, sends it home like a Refused Delivery (ADR-0007) with an additive flag in Shipping Published,
so the Home Harbor imprisons it on Arrival and frees it when its Fine is paid from its Savings, in
one local transaction (ADR-0008).

## Consequences

### Positive
- The Coast Guard sees every voyage of a ship across all Harbors, with no shared registry and no
  change to any Harbor's autonomy (ADR-0003): it is a context of its own, not a Harbor.
- Arrivals never wait for the Coast Guard: when it is down, nobody is imprisoned and the voyages
  carry on.
- Money stays local: the Fine is paid at the Home Harbor, the only one that holds the Imprisoned
  Ship, and no payment event crosses Harbors.
- Shipping Published only gains a field; ship-terminal ignores it (R-8).
- Kafka Streams' `TopologyTestDriver` tests the topology without a broker, so the component's
  default test command needs no Docker (ADR-0010).

### Negative
- A new technology with its own moving parts: a state store, changelog and repartition topics on the
  broker, and an `application.id`. Outbox messages are keyed by Shipping id, so counting by Ship Id
  needs a re-key, and the event type sits in a header, which the DSL's filters cannot read.
- The first component besides Debezium that writes to Kafka directly. Its writes are not
  transactional with any database, and a crash can publish `ship-imprisoned` twice; the Harbors'
  inbox absorbs that.
- The verdict comes after the Arrival. A ship that sails on before `ship-imprisoned` reaches the
  Harbor it arrived at escapes this time.
- A third independent copy of the Shipping Published shape and a new event shape in every Harbor
  (R-8). Like Kafka itself, the Coast Guard runs only in Compose, not in `k8s/` (R-4).
- The Home Harbor cannot rely on reading `ship-imprisoned` before the ship arrives (another topic, no
  ordering), hence the flag in Shipping Published. An Imprisoned Ship whose Home Harbor never has
  50.00 $ stays imprisoned.

### Neutral
- Contraband is recognized by Cargo name: the list belongs to the Coast Guard, and the Cargo catalog
  gains no category.
- Counting each Shipping id once and starting again after an imprisonment are rules of the CoastGuard
  context, kept in the component's state store.

## Alternatives considered
- **A checkpoint at sea, where Destination Harbors consume only the voyages the Coast Guard clears to
  a new topic**: Rejected because every Arrival would depend on the Coast Guard being up, and it
  changes which topic the Harbors consume (ADR-0004).
- **Each Harbor judges only the voyages it has seen arrive**: Rejected because every Harbor would see
  a different part of a ship's history, so "three times within 10 minutes" could not be judged.
- **A count carried with the ship in Shipping Published, like Earnings (ADR-0008)**: Rejected because
  a time window over a count that each Harbor rewrites is fragile, and it leaves Kafka Streams
  untried.
- **The Coast Guard as a plain consumer like ship-terminal**: Rejected because windowing, the state
  store and re-keying would have to be built by hand, and trying Kafka Streams is a goal.
- **The ship stays imprisoned where it was caught and its Home Harbor pays the Fine from afar**:
  Rejected because it needs a payment event between Harbors, which ADR-0008 avoided.

## References
- Domain: [CoastGuard](../domain/contexts/CoastGuard/CoastGuard.md), [Shipping glossary](../domain/contexts/Shipping/glossary.md) (Confiscation, Imprisoned Ship), [Trade glossary](../domain/contexts/Trade/glossary.md) (Fine), [shared glossary](../domain/glossary.md) (Ship Imprisoned, Shipping Published), [context map](../domain/context-map.md)
- arc42: [03 Context](../arc42/03-context.md), [04 Solution Strategy](../arc42/04-solution-strategy.md), [05 Building Blocks](../arc42/05-building-blocks.md), [06 Runtime](../arc42/06-runtime.md) (6.4, 6.7), [07 Deployment](../arc42/07-deployment.md), [08 Crosscutting](../arc42/08-crosscutting.md) (8.2, 8.3, 8.8), [11 Risks](../arc42/11-risks.md) (R-4, R-8, R-13)
- Related: ADR-0002, ADR-0003, ADR-0004, ADR-0007, ADR-0008, ADR-0010
