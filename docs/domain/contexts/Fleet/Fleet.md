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
