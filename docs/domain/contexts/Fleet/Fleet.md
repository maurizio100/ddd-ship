# Context: Fleet

The registry of ships. A ship is brought into the fleet with a Ship Name and a Catain chosen from a
fixed roster, can be renamed or removed, and is listed as one of the Available Ships. Each Catain has
a portrait (Catain Image) kept in object storage.

Terms: [`glossary.md`](glossary.md) — this context's own vocabulary, in the folder beside this file.

## Participants
- User — creates, renames and removes ships; chooses the Catain for a new ship ("Choose Your Catain").
- Catain — commands a ship; part of a fixed, seeded roster.
- Object storage (MinIO) — holds the Catain Images.

## Behaviour
- Create a ship with a Ship Name and a Catain; the Catain must exist on the roster (`ShipManagementService.createShip`).
- A Ship Name must be non-blank and shorter than 255 characters; an invalid rename keeps the old name (`Ship.shipName`).
- Rename and remove a ship (`ShipManagementService.updateShip/deleteShip`).
- List all ships and the Catain roster; fetch a Catain's portrait (`ShipInformationService`, `CatainImageService`).
- The Catain table is also streamed to Kafka by a Debezium connector (`kafka-connect/connectors/catain-connector_*.json`); no consumer exists yet.

### Ships moving between Harbors (harbor-voyages ideation, 2026-10-03)
Arrival at the Destination Harbor is built (STORY-006), and so is removing the ship from the Origin Harbor's fleet (STORY-007). Each Harbor has its own fleet.
- Every ship has a Ship Id that is not its database id and stays the same at every Harbor.
- The Catain roster is the same at every Harbor, so a ship's Catain always exists wherever it arrives. Every Harbor names each Catain by the same Catain id (seeded with fixed ids by `V9__same_reference_ids_at_every_harbor.sql`). *Built by STORY-006.*
- On Arrival the ship, with its Ship Id, Ship Name and Catain, joins the Destination Harbor's fleet, with no Loaded Cargo and no Active Shipping (`ArrivalManagementService`, Shipping). *Built by STORY-006.*
- When the Origin Harbor learns of the Arrival (Ship Arrived), it removes the ship from its fleet. The ship's record and its Shipping history are kept; it is simply no longer among the Available Ships, and every User command on it answers "not found". A later Arrival of the same Ship Id takes it back in, under the same record. *Built by STORY-007.*

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
