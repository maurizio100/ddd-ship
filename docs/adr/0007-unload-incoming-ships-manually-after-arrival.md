# ADR-0007: Unload Incoming Ships manually after Arrival

## Status
Accepted

## Date
2026-10-04

## Context
Today Arrival is fully automatic: in one transaction the Destination Harbor records the event in
its inbox (ADR-0004), puts one of each Loaded Cargo into its Stock, takes the ship into its fleet
with an empty load, and publishes Ship Arrived. The harbor economy adds a Price to every Cargo and
Savings to every Harbor: the Destination Harbor pays the Delivery Price for the Cargo it receives,
and may refuse a delivery, freely or because its Savings fall short. A Harbor cannot decide or pay
inside an event handler without a User, and an Arrival that fails for lack of Savings would block
the event and leave the Origin Harbor's Shipping open. A refused ship has to go somewhere: back to
its Home Harbor, where it was registered.

## Decision
We will keep Arrival automatic for the ship but not for its Cargo: the ship joins the Destination
Harbor's fleet as an Incoming Ship with its Cargo aboard, and Ship Arrived is published in the same
transaction as today. A User then unloads it (Cargo into Stock and Delivery Price paid from Savings,
in one transaction) or refuses it; a refused ship is Released automatically to its Home Harbor with
its Cargo aboard, through the outbox as an ordinary Shipping Published.

## Consequences

### Positive
- The protocol between Harbors is unchanged: Ship Arrived still ends the Origin Harbor's Shipping
  promptly, independent of when a User acts.
- Payment and refusal are local, User-driven decisions in one REST-triggered transaction; no event
  handler can fail for lack of Savings.
- ADR-0004 still holds: a redelivered Shipping Published cannot create a second Incoming Ship
  (inbox and `arrivals`), and the Stock increment now happens once, on the User's Unload.

### Negative
- An arrived ship must keep its Cargo (with quantities): today an arrived ship gets an empty load
  and `ships_cargos` belongs to a Shipping, so the Cargo needs a new home on the Incoming Ship.
- A new ship state: an Incoming Ship is in the fleet but cannot be prepared or Released, and must
  be distinguished from a ship that is simply idle.
- Shipping Published must carry the ship's Home Harbor so any Harbor can send a refused ship back.
- A Refused Delivery is a voyage no User Released; it repeats the return-trip ordering of R-10, and
  a ship refused by its own Home Harbor has nowhere to go (open, see below).
- Incoming Ships wait for a User indefinitely; nothing unloads or refuses them on a timer.

### Neutral
- The new harbor management page in ship-frontend lists Incoming Ships; it needs a push similar to
  ADR-0006, or refetches on the existing `ship-arrived` SSE event.
- Open: what happens when a ship's Home Harbor refuses its Cargo.

## Alternatives considered
- **Keep Unloading on Arrival automatic and charge automatically**: Rejected because the Harbor
  gets no choice to refuse, and an Arrival without enough Savings must either fail the event or
  drive Savings below 0.
- **Hold the ship outside the fleet and send Ship Arrived only after Unload or refusal**: Rejected
  because the Origin Harbor's Shipping would stay at sea for as long as a User takes, coupling two
  Harbors to one User's response time and widening R-10 and the "Harbor that never answers" gap.

## References
- Domain: [Trade glossary](../domain/contexts/Trade/glossary.md), [Shipping glossary](../domain/contexts/Shipping/glossary.md) (Incoming Ship, Refused Delivery), [shared glossary](../domain/glossary.md) (Home Harbor)
- arc42: [04 Solution Strategy](../arc42/04-solution-strategy.md), [05 Building Blocks](../arc42/05-building-blocks.md), [06 Runtime](../arc42/06-runtime.md) (6.4, 6.6), [08 Crosscutting](../arc42/08-crosscutting.md) (8.2, 8.3), [11 Risks](../arc42/11-risks.md) (R-10)
- Related: ADR-0002, ADR-0003, ADR-0004 (extended, not superseded), ADR-0005, ADR-0006
