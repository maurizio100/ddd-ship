# Glossary

## Shared / Cross-context

Terms with one identical meaning in two or more contexts.

| Term | Definition | Contexts | Notes |
|------|------------|----------|-------|
| Cargo | A good (e.g. Ale, Rum, Silk) with a name and a Weight that can be carried by a ship. | CargoLoading, Shipping, HarborTerminal, Trade | Two Cargo with the same name are considered equal. Planned (harbor-economy, 2026-10-04): every Cargo gets a Price (Trade). |
| Catain | The cat who commands a ship, chosen from a fixed roster when the ship is created. | Fleet, Shipping, HarborTerminal | Canonical. Alias: "Captain" (terminal output, `TASK_add-new-ship.md`). |
| Harbor | One running Hexagonship — a ship-backend with its own database — with its own fleet and Stock, known by its Harbor Name. | Fleet, CargoLoading, Shipping, Trade | Planned (harbor-voyages, 2026-10-03). Each backend instance is one Harbor. Not to be confused with HarborTerminal, the departure board for all Harbors. Planned (harbor-economy, 2026-10-04): a Harbor also holds Savings (Trade). |
| Home Harbor | The Harbor where a ship was registered; it never changes, wherever the ship sails. | Fleet, Shipping, Trade | Planned (harbor-economy, 2026-10-04). Travels with the ship between Harbors. Not Origin Harbor (per voyage) and not Arrived from (overwritten on every Arrival). Its Savings receive the ship's Earnings. |
| Ship | A vessel in the fleet, identified by an id, with a Ship Name and a Catain. | Fleet, CargoLoading, Shipping, HarborTerminal | |
| Ship Id | The identity of a ship that it keeps for its whole life, across every Harbor it sails to. | Fleet, Shipping | Planned (harbor-voyages). Not the database id, which is local to one Harbor. |
| Shipping | One voyage of a ship, from preparation through Release. | Shipping, HarborTerminal | |
| Shipping Published | The event announcing that a ship has been Released, carrying ship, Catain, weight, Shipping Quote and Cargo list. | Shipping, HarborTerminal | Event type `shipping-published`, topic `hexagonship-shipping`. Planned (harbor-economy, 2026-10-04): also carries how many of each Cargo, the ship's Home Harbor and its Earnings. |

## Same word, different meaning

The same word used for different things in different contexts. Both definitions
are correct in their own context — this table exists so that reading only one of
them is not a silent trap.

| Term | Context | Means | vs. Context | Means |
|------|---------|-------|-------------|-------|

## Index

Every context's own glossary.

| Context | Terms |
|---|---|
| [CargoLoading](contexts/CargoLoading/glossary.md) | Available Cargo, Stock, weights and limits |
| [Fleet](contexts/Fleet/glossary.md) | Ship registry and Catain roster |
| [HarborTerminal](contexts/HarborTerminal/glossary.md) | Departure board wording |
| [Shipping](contexts/Shipping/glossary.md) | Voyage lifecycle, Release, Sailors Code, Harbors and Arrival |
| [Trade](contexts/Trade/glossary.md) | Price, Savings, Market, Delivery Price and Earnings |
