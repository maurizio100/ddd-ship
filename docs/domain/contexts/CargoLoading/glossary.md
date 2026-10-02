# CargoLoading — glossary

Terms specific to this context. Shared terms and cross-context collisions are in
[`../../glossary.md`](../../glossary.md).

| Term | Definition | Notes |
|------|------------|-------|
| Available Cargo | The catalog of Cargo that can be loaded onto ships. | Seeded, 14 goods. |
| Current Weight | The summed Weight of all Cargo currently loaded on a ship. | Rounded to two decimals. |
| Loaded Cargo | The Cargo currently on board a ship; each Cargo at most once. | Also called cargo load. |
| Max Weight | The heaviest load a ship may carry: 15.0. | Constant `Ship.MAX_WEIGHT`; same for every ship. |
| Weight | How heavy a single Cargo is. | The terminal prints ship weight in "kg"; the unit is not otherwise stated — needs review. |
