# Fleet — glossary

Terms specific to this context. Shared terms and cross-context collisions are in
[`../../glossary.md`](../../glossary.md).

| Term | Definition | Notes |
|------|------------|-------|
| Available Ships | The list of all ships in the fleet, shown with their Catain and current shipping status. | UI heading on the ships list. |
| Catain Image | The portrait of a Catain, stored in object storage and referenced by an image id. | |
| Crew Capacity | How many Crew Members a ship can carry, set by its Ship Class (Sloop 2, Brig 4, Galleon 6). | |
| Dismiss | The User removing one Crew Member from a ship's Crew with one click. | Only while the ship is in port at its Home Harbor. Half the Hiring Fee is refunded into the Savings; a refused Dismiss refunds nothing. The Crew Member never returns to the Recruit Pool. |
| Hire | The User taking one Recruit from the Harbor's Recruit Pool into a ship's Crew with one click. | Built. Only while the ship is in port at its Home Harbor (any state but at sea), only while the Crew is below its Crew Capacity, and only if the Savings pay the Hiring Fee (Trade); otherwise refused. The Recruit becomes a Crew Member with the same identity, and a new Recruit takes its place in the Recruit Pool. A Crew Member can be dismissed (see Dismiss). |
| Recruit | A cat in a Harbor's Recruit Pool, with a UUID identity, a name, a trait and a Recruit Portrait, who can be Hired onto a ship. | Becomes a Crew Member once Hired. |
| Recruit Pool | The five Recruits a Harbor offers for Hiring. | Generated at the Harbor from its Harbor Name when it first opens; the same Recruits are offered when it opens again. Each Harbor has its own pool, unlike the Catain roster, which is the same at every Harbor. |
| Recruit Portrait | The look of a Recruit (fur colour, coat pattern, eye colour, accessory), kept by the Crew Member it becomes. | Derived from the cat's UUID alone, so identical at every Harbor ([ADR-0012](../../../adr/0012-derive-recruit-portraits-from-the-cats-identity.md)). |
| Ship Class | The size of a ship, chosen when it is created, which sets its Crew Capacity: Sloop 2, Brig 4, Galleon 6. | It does not set the Max Weight, 15.0 for every Ship Class. The User sees each Ship Class with its Crew Capacity when creating a ship. It never changes and travels with the ship in Shipping Published. A ship registered before Ship Classes existed is a Sloop. |
| Ship Name | The name a ship is known by; mandatory, non-blank, shorter than 255 characters. | |
