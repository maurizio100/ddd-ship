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
- ⚠ needs review: nothing ever sets a Shipping to `DONE`, so a released ship can never get another Shipping.
- ⚠ needs review: the Sailors Code is `mod 14` (0–13) while 15 Shipping Quotes are seeded (0–14); the last quote is unreachable.

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
_No entries yet._

### Domain services
_No entries yet._
