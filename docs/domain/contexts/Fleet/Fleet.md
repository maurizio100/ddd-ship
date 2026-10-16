# Context: Fleet

The registry of ships. A ship is brought into the fleet with a Ship Name and a Catain chosen from a
fixed roster, can be renamed or removed, and is listed as one of the Available Ships. Each Harbor has
its own fleet, which ships join on Arrival and leave when they have arrived elsewhere. Each Catain has
a portrait (Catain Image) kept in object storage.

Terms: [`glossary.md`](glossary.md) — this context's own vocabulary, in the folder beside this file.

## Strategic classification
- **Domain:** Supporting — the registry every voyage needs, but holds little logic of its own.
- **Business model:** Engagement — a playful harbor; ships and their Catains are what the User plays with.
- **Evolution:** Custom built — built for Hexagonship and stable.

## Participants
- User — creates, renames and removes ships; chooses the Catain for a new ship ("Choose Your Catain").
- Catain — commands a ship; part of a fixed, seeded roster.
- Object storage (MinIO) — holds the Catain Images.

## Behaviour
- Create a ship with a Ship Name and a Catain from the roster. Its Home Harbor is this Harbor; the fleet shows the Home Harbor on every ship card.
- Rename and remove a ship.
- List all ships and the Catain roster; fetch a Catain's portrait.
- On Arrival the ship, with its Ship Id, Ship Name and Catain, joins the Destination Harbor's fleet, with no Loaded Cargo and no Active Shipping (Shipping hands it over).
- When the Origin Harbor learns of the Arrival (Ship Arrived), it removes the ship from its fleet.
- The fleet shows where an arrived ship came from ("arrived from"): the Origin Harbor of its latest Arrival at this Harbor.
- The Catain table is also streamed to Kafka by a Debezium connector (`kafka-connect/connectors/catain-connector_*.json`); no consumer exists yet.

## Inbound communication
| From | Message | Kind |
|------|---------|------|
| Shipping | Take the arrived ship into the fleet | Command |
| Shipping | Remove the ship that arrived elsewhere from the fleet | Command |

## Outbound communication
| To | Message | Kind |
|----|---------|------|
| Object storage (MinIO) | Fetch a Catain Image | Query |
| Kafka (Debezium) | Catain changed | Event |

## Business decisions
- A new ship's Catain must be on the roster.
- A Ship Name must be non-blank and shorter than 255 characters; an invalid rename keeps the old name.
- A ship has exactly one Home Harbor: this Harbor when it is registered here, the Home Harbor carried in Shipping Published when it arrives (the Origin Harbor for an event without one). It never changes, not by a rename, a Release, an unloading or a later Arrival.
- Every ship has a Ship Id that is not its database id and stays the same at every Harbor.
- The Catain roster is the same at every Harbor, and every Harbor names each Catain by the same Catain id, so a ship's Catain exists wherever it arrives.
- A ship that left the fleet keeps its record and its Shipping history, but is no longer among the Available Ships, and every User command on it answers "not found". A later Arrival of the same Ship Id takes it back in, under the same record.
- "Arrived from" is empty for a ship registered at this Harbor, is kept while the ship is away, and is replaced by its next Arrival.
