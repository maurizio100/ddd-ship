# Fleet — glossary

Terms specific to this context. Shared terms and cross-context collisions are in
[`../../glossary.md`](../../glossary.md).

| Term | Definition | Notes |
|------|------------|-------|
| Available Ships | The list of all ships in the fleet, shown with their Catain and current shipping status. | UI heading on the ships list. |
| Catain Image | The portrait of a Catain, stored in object storage and referenced by an image id. | |
| Crew Capacity | How many Crew Members a ship can carry, set by its Ship Class (Sloop 2, Brig 4, Galleon 6). | |
| Hire | The User taking one Recruit from the Harbor's Recruit Pool into a ship's Crew with one click. | Built. Only while the ship is in port at its Home Harbor (any state but at sea), only while the Crew is below its Crew Capacity, and only if the Savings pay the Hiring Fee (Trade); otherwise refused. The Recruit becomes a Crew Member with the same identity, and a new Recruit takes its place in the Recruit Pool. Open: can a Crew Member be dismissed. |
| Recruit | A cat in a Harbor's Recruit Pool, with a UUID identity, a name and a trait, who can be Hired onto a ship. | Becomes a Crew Member once Hired. Open: do Recruits have an image. |
| Recruit Pool | The five Recruits a Harbor offers for Hiring. | Generated at the Harbor from its Harbor Name when it first opens; the same Recruits are offered when it opens again. Each Harbor has its own pool, unlike the Catain roster, which is the same at every Harbor. |
| Ship Class | The size of a ship, chosen when it is created, which sets its Crew Capacity: Sloop 2, Brig 4, Galleon 6. | It does not set the Max Weight, 15.0 for every Ship Class. It never changes and travels with the ship in Shipping Published. A ship registered before Ship Classes existed is a Sloop. |
| Ship Name | The name a ship is known by; mandatory, non-blank, shorter than 255 characters. | |
