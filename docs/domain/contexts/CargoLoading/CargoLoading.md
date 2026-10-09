# Context: CargoLoading

Putting goods on board. A fixed catalog of Cargo (Ale, Rum, Silk, …), each with a Weight, can be
loaded onto and unloaded from a ship while it is being prepared, the same Cargo more than once
included. A ship can carry at most its Max Weight, applied to the summed Weight of everything
aboard.

Terms: [`glossary.md`](glossary.md) — this context's own vocabulary, in the folder beside this file.

## Participants
- User — picks Cargo from the Available Cargo and loads or unloads it on a ship.

## Behaviour
- Each Harbor keeps its own Stock: how many of each Cargo it has on hand. A Harbor starts with its Starting Stock (3 of every Cargo, the same at every Harbor), seeded once when it opens for the first time (`V7__stocks.sql`).
- List the Available Cargo: the catalog Cargo (seeded by `V2__cargos.sql`) whose Stock at this Harbor is above 0, each with its Stock (`CargoInformationService`).
- Load Cargo onto a ship, the same Cargo more than once included: rejected if the ship would exceed its Max Weight of 15.0, applied to the summed Weight of everything aboard (`ShipTooHeavyException`) — `Ship.addCargo` — or if the Harbor's Stock holds none of it (`CargoOutOfStockException`). A successful load takes one out of the Stock. A rejected load changes neither the ship nor the Stock and is answered with `409`. *Modified by STORY-022.*
- Unload Cargo while preparing; if it was on board, the ship's Current Weight drops by the Cargo's Weight, never below 0 (`Ship.removeCargo`), and one is put back into the Stock. Unloading Cargo that isn't on board changes nothing.
- The Current Weight against the Max Weight is shown while preparing a shipping ("Current Weight: x / 15").

### Unloading on Arrival (harbor-voyages ideation, 2026-10-03)
Built by STORY-006 unless marked otherwise.
- After the Starting Stock, a Harbor's Stock is refilled only by ships delivering Cargo (and by a User unloading while preparing, which returns what was taken).
- Unloading on Arrival moves all Loaded Cargo of an arriving ship into the Destination Harbor's Stock, exactly once per Arrival: one of each Loaded Cargo through `StockRepositoryPort.putIntoStock(cargoId, 1)` (`ArrivalManagementService`, Shipping). *Built by STORY-006.*
- Every Harbor names each Cargo by the same Cargo id (seeded with fixed ids by `V9__same_reference_ids_at_every_harbor.sql`), so an arriving ship's Cargo resolves at any Harbor. *Built by STORY-006.*

## Tactical model

> Status: not yet discovered
> The tactical model emerges during implementation. Each story that
> touches this context adds or modifies entries below via the
> `implement-story` skill. Do not pre-populate from the discovery
> source — tactical choices are made at code time, not at analysis time.

### Aggregates
- **Ship** (Shared Kernel with Fleet and Shipping) — holds the Loaded Cargo and the Current Weight; enforces "at most the Max Weight" in `addCargo`, applied to the summed Weight of everything aboard, and names the Cargo in the rejection. The same Cargo may be loaded more than once. `removeCargo` removes one matching instance, returns whether the Cargo was on board, and changes the Current Weight only then. *Modified by STORY-004, STORY-022.*

### Entities (non-aggregate roots)
_No entries yet._

### Value objects
- **AvailableCargoDTO** — a catalog Cargo that can be loaded here, with its Stock (always above 0). Driving-port DTO of `CargoInformationPort`. *Introduced by STORY-004.*
- **CargoOutOfStockException** — the rule violation "the Harbor's Stock holds none of this Cargo". *Introduced by STORY-004.*

### Domain services
- **StockRepositoryPort** (driven port) — the Harbor's Stock: quantity per Cargo, one Stock per Harbor. Invariant: never below 0, enforced atomically by `takeOneFromStock` (one conditional statement, so concurrent loads of the last Cargo cannot both succeed). `putIntoStock(cargoId, quantity)` is the single entry point for refilling. Writes run in the caller's transaction. *Introduced by STORY-004.*
- **CargoLoadManagementService** — loading applies the ship's rules first, then takes one from the Stock; unloading Loaded Cargo puts one back. Each runs in one transaction with the cargo load. *Modified by STORY-004.*
- **CargoInformationService** — Available Cargo = catalog Cargo with Stock above 0, in catalog order. *Modified by STORY-004.*
