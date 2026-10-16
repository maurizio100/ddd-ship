# Context: Shipping

The voyage of a ship. A ship gets a new Shipping, is prepared (loaded), and is then Released to a
Destination Harbor: it receives a Sailors Code and a Shipping Quote, puts to sea, and its departure is
announced to the rest of the world as a Shipping Published event. Each ship-backend instance is one
Harbor; the Destination Harbor handles the Arrival and answers with Ship Arrived, which ends the
voyage at the Origin Harbor.

Terms: [`glossary.md`](glossary.md) — this context's own vocabulary, in the folder beside this file.

## Strategic classification
- **Domain:** Core — the voyage between Harbors and its events are what the project exists to try out (outbox, Kafka, at-least-once delivery).
- **Business model:** Engagement — Releasing a ship and seeing it arrive elsewhere is the heart of the game.
- **Evolution:** Custom built — built for Hexagonship; still extended by each epic.

## Participants
- User — starts a new Shipping for a ship, Releases it, and refuses Incoming Ships.
- Kafka (via the transactional outbox and Debezium) — carries Harbor Opened, Shipping Published and Ship Arrived between Harbors.

## Behaviour
- On startup a Harbor publishes Harbor Opened with its Harbor Name; every Harbor keeps the Known Harbors it has heard of.
- Create a new Shipping for a ship; it starts in `PREPARING`.
- Release the Active Shipping to a Destination Harbor the User picks from the Known Harbors. A Harbor with no Known Harbors offers no choice, so its ships cannot be Released. Releasing derives a Sailors Code from the Current Weight and the current minute, looks up the matching Shipping Quote, sets the state to `SHIPPING` with the Destination Harbor, and writes Shipping Published, naming this Harbor as Origin Harbor, to the outbox in the same transaction.
- Show the Shipping Summary after release: "{ship} left the harbor together with {Catain}!", bound for the Destination Harbor, plus the Sailors Code; the ships list then shows "{ship} is at sea".
- Arrival: the Destination Harbor receives the Shipping Published addressed to it and, without a User action, takes the ship into its fleet (Fleet) with its Cargo still aboard, as an Incoming Ship if it carries any, and publishes Ship Arrived.
- End the voyage: when the Origin Harbor learns from Ship Arrived that its ship has arrived, the Shipping becomes `DONE` and the ship leaves the Origin Harbor's fleet. The Shipping stays readable as `DONE`.
- A return trip is an ordinary new Shipping at the Destination Harbor, Released back to the former Origin Harbor (or anywhere else).
- Refuse an Incoming Ship away from its Home Harbor, whether or not the Savings could pay its Delivery Price: nothing is paid and the Stock is unchanged. In one transaction the ship stops being Incoming, gets a new Shipping with its Cargo aboard as Loaded Cargo, and is Released to its Home Harbor like any Release. At its Home Harbor it arrives as an Incoming Ship.
- Refuse an Incoming Ship at its own Home Harbor: the ship only stops being Incoming and stays in the fleet with its Cargo aboard; no Shipping is started, nothing is paid, the Stock is unchanged and nothing is published. That Cargo sails with the ship on its next Release to another Harbor.

## Inbound communication
| From | Message | Kind |
|------|---------|------|
| Shipping (another Harbor) | Harbor Opened | Event |
| Shipping (another Harbor) | Shipping Published, addressed to this Harbor | Event |
| Shipping (another Harbor) | Ship Arrived, for a ship this Harbor Released | Event |

## Outbound communication
| To | Message | Kind |
|----|---------|------|
| Shipping (other Harbors) | Harbor Opened | Event |
| Shipping (other Harbors) | Shipping Published | Event |
| Shipping (the Origin Harbor) | Ship Arrived | Event |
| HarborTerminal | Shipping Published | Event |
| Fleet | Take the arrived ship into the fleet | Command |
| Fleet | Remove the ship that arrived elsewhere from the fleet | Command |
| Trade | Pay the Delivery Price of an unloaded Incoming Ship | Command |
| Trade | Credit the Earnings of a ship arriving at its Home Harbor | Command |

## Business decisions
- A new Shipping is allowed only when the ship has no Active Shipping or the previous one is `DONE`, and never for an Incoming Ship until it is unloaded or refused.
- Only a `PREPARING` Shipping can be Released, so one Shipping is published once. A `SHIPPING` Shipping always has a Destination Harbor; a `PREPARING` one has none. `DONE` is reached only from `SHIPPING`.
- The Destination Harbor must be one of the Known Harbors; anything else is rejected before anything changes. A Harbor is never one of its own Known Harbors, so a ship cannot sail to the Harbor it is in, and a Harbor that opens again is still known only once.
- A Harbor takes in only ships Released to it.
- One Release arrives at most once: a redelivered event (same event id) and a re-publication of the same Shipping (new event id) change nothing, even after the ship sailed on and left the fleet. An Arrival whose Catain or Cargo is unknown changes nothing.
- A ship already in the fleet arrives only if it is still at sea from here (it came back before this Harbor learned of its earlier Arrival elsewhere): that earlier voyage ends and the ship stays in the fleet. A ship in the fleet that is not at sea is skipped.
- A Ship Arrived ends at most one voyage: only the Active Shipping with the reported Shipping id, still at sea. A late or repeated Ship Arrived never ends a later voyage and never fails.
- A ship has Cargo aboard only while it is not at sea; the Current Weight counts the Cargo aboard and the Loaded Cargo. An Incoming Ship always has Cargo aboard, a ship that arrives with no Cargo is not Incoming, and a ship at sea never has Cargo aboard.
- An Incoming Ship is unloaded or refused at most once: of an unloading and a refusal of the same ship, at most one takes effect.
- Cargo a Home Harbor refused stays aboard and cannot be unloaded into the Stock while preparing; it becomes Loaded Cargo on the next Release.

## Assumptions and open questions
- ⚠ needs review: the Sailors Code is `mod 14` (0–13) while 15 Shipping Quotes are seeded (0–14); the last quote is unreachable.
- Open: what a Harbor does with a Shipping Published addressed to a Harbor that never answers (no Ship Arrived).
