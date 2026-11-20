# Context Map

## Bounded Contexts

### 1. Fleet
> The registry of ships and the Catain roster: ships are created with a name and a Catain, renamed and removed.

### 2. CargoLoading
> Loading and unloading Cargo from the catalog onto a ship, within the ship's Max Weight.

### 3. Shipping
> A ship's voyage: a new Shipping is prepared and Released, getting a Sailors Code and Shipping Quote, and its departure is published.

### 4. HarborTerminal
> The board of every event between the Harbors: departures, arrivals, Harbors opening and ships imprisoned.

### 5. Trade
> A Harbor's money: Cargo Prices, the Harbor's Savings, paying for delivered Cargo, buying Cargo at the Market, and the Earnings ships collect and bring home. Candidate context; Prices, Savings, Market, Delivery Price, collecting Earnings on unloading and crediting Earnings on Arrival at the Home Harbor are built.

### 6. CoastGuard
> Watches the seas between all Harbors: counts per ship the voyages carrying Contraband and imprisons a ship that does so too often. Built: counting and publishing Ship Imprisoned. Built: the Harbor side's Confiscation. Built: the Fine, paid at the Home Harbor.

---

## Relationships

| # | Upstream | Downstream | Type | Clarity | Notes |
|---|----------|------------|------|---------|-------|
| 1 | Shipping | HarborTerminal | Open Host Service (OHS) U:OHS D:CF | assumed | Shipping Published, Ship Arrived and Harbor Opened events via transactional outbox → Debezium → Kafka; the terminal copies the event models as-is. |
| 2 | Fleet | Shipping | Shared Kernel (SK) | assumed | Both use the same `Ship` class in one backend module; Ship identity and Catain name are copied into the Shipping and its event. Planned (crew ideation, 2026-10-05): the Crew also lives on the shared `Ship`. |
| 3 | CargoLoading | Shipping | Shared Kernel (SK) | assumed | Loaded Cargo and Current Weight live on the shared `Ship` and feed the Sailors Code and the event. |
| 4 | Fleet | CargoLoading | Shared Kernel (SK) | assumed | Cargo is loaded onto the shared `Ship`. |
| 5 | Shipping | Shipping | Published Language (PL) | assumed | Future (harbor-voyages, 2026-10-03): Shipping at one Harbor talks to Shipping at another Harbor via Harbor Opened, Shipping Published (with Origin and Destination Harbor) and Ship Arrived over Kafka. Inside a Harbor, Arrival hands the ship to Fleet and its Cargo to CargoLoading in-process, through the existing Shared Kernel. Shipping Published carries the ship's Home Harbor and its Earnings, which the Arrival keeps. Planned (harbor-economy, 2026-10-04): Shipping Published also carries Cargo quantities. Built: a Refused Delivery sends the ship back to its Home Harbor as an ordinary Shipping Published. Planned (crew ideation, 2026-10-05): Shipping Published also carries the Crew in full; Arrival keeps it aboard the ship. Built: after a Confiscation the Harbor holding the ship sends it to its Home Harbor as an ordinary Shipping Published, flagged (`shipEventData.imprisoned`) so the ship is imprisoned on Arrival there. |
| 6 | CargoLoading | Trade | Customer-Supplier (CS) | assumed | Built (STORY-025, STORY-045): Cargo bought at the Market and Cargo unloaded from an Incoming Ship go into CargoLoading's Stock. |
| 7 | Trade | Shipping | Customer-Supplier (CS) | assumed | Built (STORY-045): Shipping asks Trade to pay the Delivery Price when an Incoming Ship is unloaded (refused if Savings fall short). In the same unloading, the Delivery Price becomes the ship's Earnings away from its Home Harbor, or goes back into the Savings at once at its Home Harbor. An Arrival at the ship's Home Harbor credits the Earnings it carries to the Savings. Built: Shipping asks Trade to pay the Fine that frees an Imprisoned Ship (refused if Savings fall short; the ship stays imprisoned). |
| 8 | Trade | Fleet | Customer-Supplier (CS) | assumed | Planned (crew ideation, 2026-10-05). Fleet asks Trade to pay the Hiring Fee from the Harbor's Savings when a Recruit is Hired (refused if Savings fall short). |
| 9 | Shipping | CoastGuard | Open Host Service (OHS) U:OHS D:CF | assumed | Built: the Coast Guard reads every Shipping Published from Kafka and copies the event model as-is, like HarborTerminal; it recognises Contraband by Cargo name. |
| 10 | CoastGuard | Shipping | Open Host Service (OHS) U:OHS D:CF | assumed | Built (Confiscation and Fine). The Coast Guard publishes Ship Imprisoned; Shipping at the Harbor holding the ship carries out the Confiscation as told, and the Home Harbor imprisons the ship on its flagged Arrival. |
| 11 | CoastGuard | HarborTerminal | Open Host Service (OHS) U:OHS D:CF | assumed | The Coast Guard publishes Ship Imprisoned directly to Kafka; the terminal copies the event model as-is and announces it. |

---

## Summary

- **Total Bounded Contexts:** 6
- **Total Relationships:** 11
- **Relationship Breakdown:** Open Host Service: 4, Customer-Supplier: 3, Shared Kernel: 3, Published Language: 1
