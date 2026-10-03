# Context: Shipping

The voyage of a ship. A ship gets a new Shipping, is prepared (loaded), and is then Released: it
receives a Sailors Code and a Shipping Quote, puts to sea, and its departure is announced to the rest
of the world as a Shipping Published event.

Terms: [`glossary.md`](glossary.md) — this context's own vocabulary, in the folder beside this file.

## Participants
- User — starts a new shipping for a ship and Releases it ("Start Journey").
- Kafka (via the transactional outbox and Debezium) — carries the Shipping Published event out.

## Behaviour
- Create a new Shipping for a ship; allowed only when it has no Active Shipping or the previous one is `DONE` (`Ship.createNewShipping`). The new Shipping starts in `PREPARING`.
- Release the Active Shipping to a Destination Harbor: the User picks it from the Known Harbors, and a Harbor that is not one of them is rejected before anything changes (the ship is still being prepared). A Harbor with no Known Harbors offers no choice, so its ships cannot be Released. A ship can only be Released while its Shipping is `PREPARING`: a ship already at sea (or without a Shipping) is rejected with `ShippingNotPreparingException` before anything is written, so one Shipping is published once. Otherwise derive a Sailors Code from the Current Weight and the current minute, look up the matching Shipping Quote, set the state to `SHIPPING` with the Destination Harbor, and write a Shipping Published event naming the current Harbor as Origin Harbor and the Destination Harbor to the outbox in the same transaction (`ShippingManagementService.releaseShipping`). *Built by STORY-005.*
- Show the Shipping Summary after release: "{ship} left the harbor together with {Catain}!", bound for the Destination Harbor, plus the Sailors Code; the ships list then shows "{ship} is at sea".
- End the voyage: when the Origin Harbor learns from Ship Arrived that its ship has arrived, the Shipping becomes `DONE` and the ship leaves the Origin Harbor's fleet (`ArrivalManagementService.receiveShipArrived`). The ship's record and its Shippings are kept, so the Shipping stays readable as `DONE`. *Built by STORY-007.*
- ⚠ needs review: the Sailors Code is `mod 14` (0–13) while 15 Shipping Quotes are seeded (0–14); the last quote is unreachable.

### Planned: voyages between Harbors (harbor-voyages ideation, 2026-10-03)
Not implemented yet unless marked as built. Each ship-backend instance is one Harbor; ships sail from one Harbor to another.
- On startup a Harbor publishes Harbor Opened with its Harbor Name; every Harbor keeps the Known Harbors it has heard of.
- Release names a Destination Harbor — one of the Known Harbors, not the current one. Shipping Published carries the Origin Harbor and the Destination Harbor. *Built by STORY-005.*
- Arrival: the Destination Harbor receives the Shipping Published addressed to it and, without a User action, unloads the ship's Cargo into its Stock (Unloading on Arrival, CargoLoading) and takes the ship into its fleet (Fleet). It then publishes Ship Arrived. *Built by STORY-006* (the Destination side; the Origin side below by STORY-007).
- The Origin Harbor receives Ship Arrived, sets the Shipping to `DONE` and removes the ship from its fleet. Only the ship's Active Shipping with the reported Shipping id, still at sea, ends, so a late Ship Arrived never ends a later voyage. The ship's row is kept as history. *Built by STORY-007.*
- A return trip is an ordinary new Shipping at the Destination Harbor, Released back to the former Origin Harbor (or anywhere else). *Built by STORY-007* (the former Origin Harbor takes the same Ship Id back into its fleet).
- Events are delivered at least once: a repeated Shipping Published must not unload Cargo twice (*built by STORY-006*), and a repeated Ship Arrived must not fail (*built by STORY-007*).
- Open: what a Harbor does with a Shipping Published addressed to a Harbor that never answers (no Ship Arrived).

## Tactical model

> Status: not yet discovered
> The tactical model emerges during implementation. Each story that
> touches this context adds or modifies entries below via the
> `implement-story` skill. Do not pre-populate from the discovery
> source — tactical choices are made at code time, not at analysis time.

### Aggregates
- **Ship** (Shared Kernel root with Fleet and CargoLoading, ADR-0001) — `release(shippingQuote, destinationHarbor)` Releases the Active Shipping to the named Destination Harbor. *Modified by STORY-005.* `endShipping(shippingId): Boolean` ends the voyage when the ship has arrived at its Destination Harbor: only if the Active Shipping has that id and is `SHIPPING`; otherwise it returns `false` and changes nothing. Leaving the fleet is not model state but a repository concern (`ShipRepositoryPort.removeFromFleet`): a ship that left is not found through the fleet queries. *Modified by STORY-007.*

### Entities (non-aggregate roots)
- **Shipping** — one voyage of a `Ship`, reached only through it; identity `ShippingId` (UUID). Holds the Shipping State, the Shipping Quote and the Destination Harbor (`HarborName`), the last two set together on Release. Invariant: a `SHIPPING` Shipping always has a Destination Harbor; a `PREPARING` one has none; only a `PREPARING` Shipping can be Released (`Ship.release` throws `ShippingNotPreparingException` otherwise; *Modified by STORY-005 review*). *Introduced by STORY-005.* `end()` sets it to `DONE`; `DONE` is reached only from `SHIPPING`, through `Ship.endShipping`. *Modified by STORY-007.*

### Value objects
- **EventId** — identity of an event consumed from another Harbor: the publishing Harbor's outbox `message_id` (UUID). A Harbor records each consumed EventId through the driven port `InboxRepositoryPort`, in the same transaction as the state change the event causes, so a redelivered event never takes effect twice (ADR-0004). Technical identity, not a ubiquitous-language term. *Introduced by STORY-001.*
- **ShippingPublishedDTO** — driving-port DTO of `ArrivalManagementPort`: Ship Id, Ship Name, Catain id, Shipping id, Cargo ids, and the Origin and Destination Harbor (both nullable: events published before STORY-005 carry none). *Introduced by STORY-006.*
- **ShipArrivedDTO** — driving-port DTO of `ArrivalManagementPort.receiveShipArrived`: Ship Id, Shipping id, and the Origin and Destination Harbor (both nullable `HarborName`). *Introduced by STORY-007.*
- **HarborName** — the unique name a Harbor is known by among all Harbors; must not be blank. The current Harbor's name comes from `harbor.name` (one Harbor per ship-backend instance, ADR-0003). Equality by name. *Introduced by STORY-003.*

### Domain services
- **HarborManagementService** (implements `HarborManagementPort`) — `openHarbor` writes Harbor Opened with the current Harbor Name to the outbox through `HarborOutboxRepositoryPort`, once per startup. `learnAboutHarbor` records the EventId through `InboxRepositoryPort` first (an already consumed event has no effect), ignores the Harbor's own name, and adds any other Harbor to the Known Harbors through `KnownHarborRepositoryPort`, which stores each Harbor Name at most once. Invariants: a Harbor is never one of its own Known Harbors; a Harbor that opens again (new EventId) is still known only once. Known Harbors is a set of Harbor Names, not an aggregate. *Introduced by STORY-003.*
- **HarborInformationService** (implements `HarborInformationPort`) — gives the current Harbor Name and its Known Harbors, ordered by name. *Introduced by STORY-003.*
- **ShippingManagementService** (implements `ShippingManagementPort`) — `releaseShipping` accepts only a Known Harbor as Destination Harbor (read through `KnownHarborRepositoryPort`) and throws `UnknownHarborException` before any write otherwise. Since a Harbor is never one of its own Known Harbors, this also rules out sailing to the current Harbor. It then Releases the ship, saves the Shipping and writes Shipping Published with the current Harbor as Origin Harbor, in one transaction. *Introduced by STORY-005.*
- **ArrivalManagementService** (implements `ArrivalManagementPort`) — handles Shipping Published, consumed from `hexagonship-shipping` by `ShippingEventListener`. In one transaction and in this order: records the EventId through `InboxRepositoryPort` (an already consumed event has no effect); ignores an event whose Destination Harbor is not the current Harbor (another Harbor's ship, or an event without one); skips a ship whose Ship Id is already in this Harbor's fleet; resolves the Catain and every Loaded Cargo by id and fails with `IllegalStateException` before any change if one is unknown; puts one of each Loaded Cargo into the Stock (`StockRepositoryPort.putIntoStock`); takes the ship into the fleet with its Ship Id, Ship Name and Catain and no Shipping or Loaded Cargo; and writes Ship Arrived through `ShippingOutboxRepository.announceShipArrived`. Ship Arrived is `event_type = 'ship-arrived'`, `aggregate_type = 'shipping'` (topic `hexagonship-shipping`), keyed by the Shipping id like the Shipping Published it answers; payload `ShipArrivedEvent` {shipId, shipName, shippingId, originHarbor, destinationHarbor}. Two guards: the inbox drops a redelivery (same EventId); the Ship Id in the fleet is the business key that drops a re-publication under a new EventId, since a ship at sea is in no fleet until it arrives. Invariants: an Arrival takes effect at most once per ship; a Harbor never takes in a ship Released to another Harbor. *Introduced by STORY-006.* `receiveShipArrived` is the Origin side, for Ship Arrived on the same topic. In one transaction and in this order: records the EventId (an already consumed event has no effect); ignores an event whose Origin Harbor is not the current Harbor; ignores a ship not in this Harbor's fleet (it already left, or was deleted); asks the ship to `endShipping` the reported Shipping and stops if it does not end; persists `DONE` (`ShippingRepositoryPort.updateActiveShipping`); and takes the ship out of the fleet (`ShipRepositoryPort.removeFromFleet`, in the caller's transaction). The "already in the fleet" guard of the Destination side reads only ships in the fleet, so a ship that left and comes back is taken in again under its Ship Id. Invariants: a Ship Arrived ends at most one voyage, never a later one, and never fails on a repetition. *Modified by STORY-007.*
