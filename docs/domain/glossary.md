# Glossary

## Shared / Cross-context

Terms with one identical meaning in two or more contexts.

| Term | Definition | Contexts | Notes |
|------|------------|----------|-------|
| Cargo | A good (e.g. Ale, Rum, Silk) with a name and a Weight that can be carried by a ship. | CargoLoading, Shipping, HarborTerminal | Two Cargo with the same name are considered equal. |
| Catain | The cat who commands a ship, chosen from a fixed roster when the ship is created. | Fleet, Shipping, HarborTerminal | Canonical. Alias: "Captain" (terminal output, `TASK_add-new-ship.md`). |
| Ship | A vessel in the fleet, identified by an id, with a Ship Name and a Catain. | Fleet, CargoLoading, Shipping, HarborTerminal | |
| Shipping | One voyage of a ship, from preparation through Release. | Shipping, HarborTerminal | |
| Shipping Published | The event announcing that a ship has been Released, carrying ship, Catain, weight, Shipping Quote and Cargo list. | Shipping, HarborTerminal | Event type `shipping-published`, topic `hexagonship-shipping`. |

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
| [CargoLoading](contexts/CargoLoading/glossary.md) | Available Cargo, weights and limits |
| [Fleet](contexts/Fleet/glossary.md) | Ship registry and Catain roster |
| [HarborTerminal](contexts/HarborTerminal/glossary.md) | Departure board wording |
| [Shipping](contexts/Shipping/glossary.md) | Voyage lifecycle, Release, Sailors Code |
