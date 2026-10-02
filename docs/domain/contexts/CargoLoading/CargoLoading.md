# Context: CargoLoading

Putting goods on board. A fixed catalog of Cargo (Ale, Rum, Silk, …), each with a Weight, can be
loaded onto and unloaded from a ship while it is being prepared. A ship can carry at most its Max
Weight, and the same Cargo cannot be loaded twice.

Terms: [`glossary.md`](glossary.md) — this context's own vocabulary, in the folder beside this file.

## Participants
- User — picks Cargo from the Available Cargo and loads or unloads it on a ship.

## Behaviour
- List the Available Cargo catalog (`CargoInformationService`; seeded by `V2__cargos.sql`).
- Load Cargo onto a ship: rejected if already loaded (`ItemAlreadyLoadedException`) or if the ship would exceed its Max Weight of 15.0 (`ShipTooHeavyException`) — `Ship.addCargo`. A rejected load returns the unchanged ship.
- Unload Cargo; the ship's Current Weight drops by the Cargo's Weight, never below 0 (`Ship.removeCargo`).
- The Current Weight against the Max Weight is shown while preparing a shipping ("Current Weight: x / 15").

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
