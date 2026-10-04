# Context Map

## Bounded Contexts

### 1. Fleet
> The registry of ships and the Catain roster: ships are created with a name and a Catain, renamed and removed.

### 2. CargoLoading
> Loading and unloading Cargo from the catalog onto a ship, within the ship's Max Weight.

### 3. Shipping
> A ship's voyage: a new Shipping is prepared and Released, getting a Sailors Code and Shipping Quote, and its departure is published.

### 4. HarborTerminal
> The harbor's departure board, announcing every ship that has put to sea.

### 5. Trade
> A Harbor's money: Cargo Prices, the Harbor's Savings, paying for delivered Cargo, buying Cargo at the Market, and the Earnings ships bring home. Candidate (harbor-economy, 2026-10-04), not built yet.

---

## Relationships

| # | Upstream | Downstream | Type | Clarity | Notes |
|---|----------|------------|------|---------|-------|
| 1 | Shipping | HarborTerminal | Open Host Service (OHS) U:OHS D:CF | assumed | Shipping Published event via transactional outbox → Debezium → Kafka; the terminal copies the event model as-is. |
| 2 | Fleet | Shipping | Shared Kernel (SK) | assumed | Both use the same `Ship` class in one backend module; Ship identity and Catain name are copied into the Shipping and its event. |
| 3 | CargoLoading | Shipping | Shared Kernel (SK) | assumed | Loaded Cargo and Current Weight live on the shared `Ship` and feed the Sailors Code and the event. |
| 4 | Fleet | CargoLoading | Shared Kernel (SK) | assumed | Cargo is loaded onto the shared `Ship`. |
| 5 | Shipping | Shipping | Published Language (PL) | assumed | Future (harbor-voyages, 2026-10-03): Shipping at one Harbor talks to Shipping at another Harbor via Harbor Opened, Shipping Published (with Origin and Destination Harbor) and Ship Arrived over Kafka. Inside a Harbor, Arrival hands the ship to Fleet and its Cargo to CargoLoading in-process, through the existing Shared Kernel. Planned (harbor-economy, 2026-10-04): Shipping Published also carries Cargo quantities, the Home Harbor and the ship's Earnings; a Refused Delivery sends the ship back to its Home Harbor. |
| 6 | CargoLoading | Trade | Customer-Supplier (CS) | assumed | Planned (harbor-economy, 2026-10-04): Cargo bought at the Market and Cargo unloaded from an Incoming Ship go into CargoLoading's Stock. |
| 7 | Trade | Shipping | Customer-Supplier (CS) | assumed | Planned (harbor-economy, 2026-10-04): Shipping asks Trade to pay the Delivery Price when an Incoming Ship is unloaded (refused if Savings fall short) and to credit Earnings to Savings when a ship reaches its Home Harbor. |

---

## Summary

- **Total Bounded Contexts:** 5
- **Total Relationships:** 7
- **Relationship Breakdown:** Shared Kernel: 3, Customer-Supplier: 2, Open Host Service: 1, Published Language: 1
