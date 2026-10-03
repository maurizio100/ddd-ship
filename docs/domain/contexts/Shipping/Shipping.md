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
- Release the Active Shipping: derive a Sailors Code from the Current Weight and the current minute, look up the matching Shipping Quote, set the state to `SHIPPING`, and write a Shipping Published event to the outbox in the same transaction (`ShippingManagementService.releaseShipping`).
- Show the Shipping Summary after release: "{ship} left the harbor together with {Catain}!" plus the Sailors Code; the ships list then shows "{ship} is at sea".
- ⚠ needs review: nothing ever sets a Shipping to `DONE`, so a released ship can never get another Shipping. Addressed by the planned Arrival below.
- ⚠ needs review: the Sailors Code is `mod 14` (0–13) while 15 Shipping Quotes are seeded (0–14); the last quote is unreachable.

### Planned: voyages between Harbors (harbor-voyages ideation, 2026-10-03)
Not implemented yet. Each ship-backend instance is one Harbor; ships sail from one Harbor to another.
- On startup a Harbor publishes Harbor Opened with its Harbor Name; every Harbor keeps the Known Harbors it has heard of.
- Release names a Destination Harbor — one of the Known Harbors, not the current one. Shipping Published carries the Origin Harbor and the Destination Harbor.
- Arrival: the Destination Harbor receives the Shipping Published addressed to it and, without a User action, unloads the ship's Cargo into its Stock (Unloading on Arrival, CargoLoading) and takes the ship into its fleet (Fleet). It then publishes Ship Arrived.
- The Origin Harbor receives Ship Arrived, sets the Shipping to `DONE` and removes the ship from its fleet.
- A return trip is an ordinary new Shipping at the Destination Harbor, Released back to the former Origin Harbor (or anywhere else).
- Events are delivered at least once: a repeated Shipping Published must not unload Cargo twice, and a repeated Ship Arrived must not fail.
- Open: what a Harbor does with a Shipping Published addressed to a Harbor that never answers (no Ship Arrived).

## Tactical model

> Status: not yet discovered
> The tactical model emerges during implementation. Each story that
> touches this context adds or modifies entries below via the
> `implement-story` skill. Do not pre-populate from the discovery
> source — tactical choices are made at code time, not at analysis time.

### Aggregates
_No entries yet._

### Entities (non-aggregate roots)
_No entries yet._

### Value objects
- **EventId** — identity of an event consumed from another Harbor: the publishing Harbor's outbox `message_id` (UUID). A Harbor records each consumed EventId through the driven port `InboxRepositoryPort`, in the same transaction as the state change the event causes, so a redelivered event never takes effect twice (ADR-0004). Technical identity, not a ubiquitous-language term. *Introduced by STORY-001.*
- **HarborName** — the unique name a Harbor is known by among all Harbors; must not be blank. The current Harbor's name comes from `harbor.name` (one Harbor per ship-backend instance, ADR-0003). Equality by name. *Introduced by STORY-003.*

### Domain services
- **HarborManagementService** (implements `HarborManagementPort`) — `openHarbor` writes Harbor Opened with the current Harbor Name to the outbox through `HarborOutboxRepositoryPort`, once per startup. `learnAboutHarbor` records the EventId through `InboxRepositoryPort` first (an already consumed event has no effect), ignores the Harbor's own name, and adds any other Harbor to the Known Harbors through `KnownHarborRepositoryPort`, which stores each Harbor Name at most once. Invariants: a Harbor is never one of its own Known Harbors; a Harbor that opens again (new EventId) is still known only once. Known Harbors is a set of Harbor Names, not an aggregate. *Introduced by STORY-003.*
- **HarborInformationService** (implements `HarborInformationPort`) — gives the current Harbor Name and its Known Harbors, ordered by name. *Introduced by STORY-003.*
