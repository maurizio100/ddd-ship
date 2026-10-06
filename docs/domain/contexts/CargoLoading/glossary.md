# CargoLoading — glossary

Terms specific to this context. Shared terms and cross-context collisions are in
[`../../glossary.md`](../../glossary.md).

| Term | Definition | Notes |
|------|------------|-------|
| Available Cargo | The Cargo that can be loaded onto ships at the current Harbor: the catalog Cargo whose Stock there is above 0. | The catalog (seeded, 14 goods) is the list of Cargo kinds; Available Cargo is shown with its Stock. |
| Current Weight | The summed Weight of all Cargo currently loaded on a ship. | Rounded to two decimals. |
| Loaded Cargo | The Cargo currently on board a ship; each Cargo at most once. | Also called cargo load. Planned change (harbor-economy, 2026-10-04): a ship may carry more than one of the same Cargo; the Max Weight still applies to the summed Weight. |
| Max Weight | The heaviest load a ship may carry: 15.0. | Constant `Ship.MAX_WEIGHT`; same for every ship. |
| Starting Stock | The Stock a Harbor begins with, before any ship has delivered Cargo to it. | 3 of every Cargo, the same at every Harbor; seeded by `V7__stocks.sql` once, when the Harbor opens for the first time. |
| Stock | How many of each Cargo a Harbor has on hand. Loading Cargo takes one out; unloading it while preparing and Unloading on Arrival put Cargo back in. | One Stock per Harbor; never below 0. Apart from the Starting Stock and Cargo returned while preparing, it is refilled only by ship deliveries (Unloading on Arrival, planned). Planned change (harbor-economy, 2026-10-04): also refilled by buying Cargo at the Market (Trade). Planned (2026-10-07, STORY-028): Cargo a Home Harbor refused (Refused Delivery, Shipping) cannot be unloaded into its Stock while preparing; Cargo loaded there still can. |
| Unloading on Arrival | Moving all Loaded Cargo of an arriving ship into the Destination Harbor's Stock. | Planned (harbor-voyages). Not the same as a User unloading a Cargo while preparing (`Ship.removeCargo`). See Arrival (Shipping). Planned change (harbor-economy, 2026-10-04): no longer automatic; the Destination Harbor unloads an Incoming Ship by hand and pays the Delivery Price (Trade), or refuses it (Refused Delivery, Shipping). |
| Weight | How heavy a single Cargo is. | The terminal prints ship weight in "kg"; the unit is not otherwise stated — needs review. |
