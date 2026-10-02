# Shipping — glossary

Terms specific to this context. Shared terms and cross-context collisions are in
[`../../glossary.md`](../../glossary.md).

| Term | Definition | Notes |
|------|------------|-------|
| Active Shipping | The one Shipping a ship currently has, if any. | A ship has at most one. |
| at sea | A ship whose Active Shipping has been Released and is underway. | UI wording ("is at sea"); corresponds to state `SHIPPING`. |
| Release | Sending a prepared ship off on its Shipping: it gets a Sailors Code and Shipping Quote and moves to `SHIPPING`. | Canonical. Aliases: "Start Journey" (UI button), "Disembark" (frontend service / summary). |
| Sailors Code | A number derived from the ship's Current Weight and the current minute, used to pick the Shipping Quote. | `(weight × minute) mod 14`. |
| Shipping Quote | A seafaring quote assigned to a Shipping on Release. | Also called Quote; 15 seeded. |
| Shipping State | The stage of a Shipping: `IDLE`, `PREPARING`, `SHIPPING`, `DONE`. | `IDLE` means "no Active Shipping"; `DONE` is never set — needs review. |
| Shipping Summary | What the user sees after Release: ship, Catain, Loaded Cargo, Weight and Sailors Code. | |
