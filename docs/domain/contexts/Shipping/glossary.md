# Shipping — glossary

Terms specific to this context. Shared terms and cross-context collisions are in
[`../../glossary.md`](../../glossary.md).

| Term | Definition | Notes |
|------|------------|-------|
| Active Shipping | The one Shipping a ship currently has, if any. | A ship has at most one. |
| Arrival | A ship reaching its Destination Harbor: that Harbor receives the Shipping Published addressed to it, unloads the ship's Cargo into its Stock and takes the ship into its fleet. | Built by STORY-006 (harbor-voyages). Triggered by the event, not by a User. See Unloading on Arrival (CargoLoading). Planned change (harbor-economy, 2026-10-04): the ship still joins the fleet and Ship Arrived is still sent, but its Cargo stays aboard: it is an Incoming Ship until unloaded or refused. |
| Arrived from | The Origin Harbor of a ship's latest Arrival at this Harbor, as shown in the fleet. | Built by STORY-013. None for a ship registered at this Harbor. Field `arrivedFrom` in the ship list and details. |
| at sea | A ship whose Active Shipping has been Released and is underway. | UI wording ("is at sea"); corresponds to state `SHIPPING`. |
| Destination Harbor | The Harbor a ship is Released to; named on Release and carried in Shipping Published. | Built by STORY-005 (harbor-voyages). Must be one of the Known Harbors and not the current Harbor. |
| Harbor Name | The unique name a Harbor is known by among all Harbors. | Built by STORY-003 (harbor-voyages). Configured per instance. |
| Harbor Opened | The event a Harbor publishes when it starts, announcing its Harbor Name to all other Harbors. | Built by STORY-003 (harbor-voyages). Event type `harbor-opened`, compacted topic `hexagonship-harbor`. |
| Incoming Ship | A ship that has arrived and joined this Harbor's fleet with Cargo still aboard, waiting to be unloaded or refused. | Planned (harbor-economy, 2026-10-04). Cannot be prepared or Released until its Cargo is unloaded or refused. Dealt with on the harbor management page. |
| Known Harbors | The Harbors this Harbor has learned of from Harbor Opened events; the choices for a Destination Harbor. | Built by STORY-003 (harbor-voyages). |
| Origin Harbor | The Harbor a ship was Released from. | Built by STORY-005 (harbor-voyages). Carried in Shipping Published so the Destination Harbor knows whom to answer. |
| Refused Delivery | A Harbor declining the Cargo of an Incoming Ship; the ship then sails back to its Home Harbor with its Cargo aboard, without a User Releasing it. | Planned (harbor-economy, 2026-10-04). Happens when the Harbor cannot pay the Delivery Price (Trade), and may also be chosen freely even when it could. Open: what happens when a ship's own Home Harbor refuses it. |
| Release | Sending a prepared ship off on its Shipping: it gets a Sailors Code and Shipping Quote and moves to `SHIPPING`. | Canonical. Aliases: "Disembark" (frontend service / summary). Departure only — never used for Arrival. "Start Journey" is a former UI label, replaced by Release in STORY-021. A return trip is a new Shipping Released back to the former Origin Harbor. |
| Sailors Code | A number derived from the ship's Current Weight and the current minute, used to pick the Shipping Quote. | `(weight × minute) mod 14`. |
| Ship Arrived | The event a Destination Harbor publishes after an Arrival, telling the Origin Harbor that the voyage is over. | Built by STORY-006 (published by the Destination Harbor). Event type `ship-arrived` on `hexagonship-shipping`. Built by STORY-007: the Origin Harbor then sets the Shipping to `DONE` and removes the ship from its fleet. |
| Shipping Quote | A seafaring quote assigned to a Shipping on Release. | Also called Quote; 15 seeded. A saying, not money — not a Price (Trade). |
| Shipping State | The stage of a Shipping: `IDLE`, `PREPARING`, `SHIPPING`, `DONE`. | `IDLE` means "no Active Shipping". Set to `DONE` when the Origin Harbor receives Ship Arrived (STORY-007); a ship whose Shipping is `DONE` can get a new one. |
| Shipping Summary | What the user sees after Release: ship, Catain, Destination Harbor, Loaded Cargo, Weight and Sailors Code. | Destination Harbor shown since STORY-005. |
