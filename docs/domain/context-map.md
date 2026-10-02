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

---

## Relationships

| # | Upstream | Downstream | Type | Clarity | Notes |
|---|----------|------------|------|---------|-------|
| 1 | Shipping | HarborTerminal | Open Host Service (OHS) U:OHS D:CF | assumed | Shipping Published event via transactional outbox → Debezium → Kafka; the terminal copies the event model as-is. |
| 2 | Fleet | Shipping | Shared Kernel (SK) | assumed | Both use the same `Ship` class in one backend module; Ship identity and Catain name are copied into the Shipping and its event. |
| 3 | CargoLoading | Shipping | Shared Kernel (SK) | assumed | Loaded Cargo and Current Weight live on the shared `Ship` and feed the Sailors Code and the event. |
| 4 | Fleet | CargoLoading | Shared Kernel (SK) | assumed | Cargo is loaded onto the shared `Ship`. |

---

## Summary

- **Total Bounded Contexts:** 4
- **Total Relationships:** 4
- **Relationship Breakdown:** Shared Kernel: 3, Open Host Service: 1
