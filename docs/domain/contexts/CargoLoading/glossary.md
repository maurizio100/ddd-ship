# CargoLoading — glossary

Terms specific to this context. Shared terms and cross-context collisions are in
[`../../glossary.md`](../../glossary.md).

| Term | Definition | Notes |
|------|------------|-------|
| Available Cargo | The catalog of Cargo that can be loaded onto ships. | Seeded, 14 goods. Planned (harbor-voyages, 2026-10-03): the catalog stays as the list of Cargo kinds; what can be loaded becomes the Cargo with Stock above 0 at the current Harbor. |
| Current Weight | The summed Weight of all Cargo currently loaded on a ship. | Rounded to two decimals. |
| Loaded Cargo | The Cargo currently on board a ship; each Cargo at most once. | Also called cargo load. |
| Max Weight | The heaviest load a ship may carry: 15.0. | Constant `Ship.MAX_WEIGHT`; same for every ship. |
| Starting Stock | The Stock a Harbor begins with, before any ship has delivered Cargo to it. | Planned (harbor-voyages). How it is defined per Harbor — needs review. |
| Stock | How many of each Cargo a Harbor has on hand. Loading Cargo takes one out; unloading it while preparing and Unloading on Arrival put Cargo back in. | Planned (harbor-voyages). One Stock per Harbor. Apart from the Starting Stock, it is refilled only by ship deliveries. |
| Unloading on Arrival | Moving all Loaded Cargo of an arriving ship into the Destination Harbor's Stock. | Planned (harbor-voyages). Not the same as a User unloading a Cargo while preparing (`Ship.removeCargo`). See Arrival (Shipping). |
| Weight | How heavy a single Cargo is. | The terminal prints ship weight in "kg"; the unit is not otherwise stated — needs review. |
