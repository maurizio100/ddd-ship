# Context: CargoLoading

Putting goods on board. A fixed catalog of Cargo (Ale, Rum, Silk, …), each with a Weight, can be
loaded onto and unloaded from a ship while it is being prepared, the same Cargo more than once
included. A ship can carry at most its Max Weight, applied to the summed Weight of everything
aboard. Each Harbor keeps its own Stock of Cargo.

Terms: [`glossary.md`](glossary.md) — this context's own vocabulary, in the folder beside this file.

## Strategic classification
- **Domain:** Supporting — loading feeds the voyage and the Stock feeds Trade, but the rules are simple.
- **Business model:** Engagement — choosing what to load is part of the game.
- **Evolution:** Custom built — built for Hexagonship and stable.

## Participants
- User — picks Cargo from the Available Cargo and loads or unloads it on a ship.

## Behaviour
- Each Harbor keeps its own Stock: how many of each Cargo it has on hand. A Harbor starts with its Starting Stock (3 of every Cargo, the same at every Harbor), seeded once when it opens for the first time.
- List the Available Cargo: the catalog Cargo whose Stock at this Harbor is above 0, each with its Stock.
- The Harbor's Stock overview lists the whole catalog with each Cargo's Stock, Cargo at 0 included (`GET /web/stock`, shown on the harbor management page).
- Load Cargo onto a ship while preparing; a successful load takes one out of the Stock. A rejected load is answered with `409`.
- Unload Cargo while preparing; if it was on board, the ship's Current Weight drops by the Cargo's Weight and one is put back into the Stock.
- The Current Weight against the Max Weight is shown while preparing a shipping ("Current Weight: x / 15").
- After the Starting Stock, the Stock is refilled only by a User unloading an Incoming Ship, by buying at the Market (both through Trade), and by a User unloading while preparing, which returns what was taken. An Arrival leaves the Stock unchanged: the Cargo stays aboard the Incoming Ship.

## Inbound communication
| From | Message | Kind |
|------|---------|------|
| Trade | Put Cargo into the Stock (bought at the Market, or unloaded from an Incoming Ship) | Command |

## Outbound communication
None: Shipping reads the Loaded Cargo and the Current Weight from the shared `Ship` (Shared Kernel), not through a message.

## Business decisions
- A ship carries at most its Max Weight of 15.0, applied to the summed Weight of everything aboard. A load that would exceed it is rejected and names the Cargo. The same Cargo may be loaded more than once.
- A load is rejected when the Harbor's Stock holds none of that Cargo. A rejected load changes neither the ship nor the Stock.
- The Stock never drops below 0, even when two loads of the last Cargo run at the same time.
- Unloading removes one matching Cargo and never takes the Current Weight below 0. Unloading Cargo that is not on board changes nothing.
- Every Harbor names each Cargo by the same Cargo id, so an arriving ship's Cargo resolves at any Harbor.
