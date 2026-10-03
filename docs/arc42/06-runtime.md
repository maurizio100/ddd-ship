> Status: draft

# 6. Runtime View

## 6.1 Create a ship

```mermaid
sequenceDiagram
    actor U as User
    participant FE as ship-frontend
    participant BE as ship-backend
    participant DB as PostgreSQL
    U->>FE: enter Ship Name, choose Catain
    FE->>BE: POST /web/ships
    BE->>DB: find Catain by id
    BE->>BE: new Ship (validate name)
    BE->>DB: save ship
    BE-->>FE: ship overview
```

If the Catain does not exist, creation fails (`IllegalStateException`).

## 6.2 Load cargo

```mermaid
sequenceDiagram
    actor U as User
    participant FE as ship-frontend
    participant BE as ship-backend
    participant DB as PostgreSQL
    U->>FE: click a Cargo
    FE->>BE: POST /web/ships/{id}/cargos
    BE->>DB: load ship and cargo
    BE->>BE: Ship.addCargo (Max Weight 15.0, no duplicates)
    BE->>DB: update cargo load
    BE-->>FE: ship details (weight / max weight)
```

A rejected load (too heavy, already loaded) returns the unchanged ship details with HTTP 200, not an error.

## 6.3 Release a shipping (transactional outbox)

Publication pattern per [ADR-0002](../adr/0002-transactional-outbox-via-debezium.md).

```mermaid
sequenceDiagram
    actor U as User
    participant FE as ship-frontend
    participant BE as ship-backend
    participant DB as PostgreSQL
    participant CDC as Debezium
    participant K as Kafka
    participant T as ship-terminal
    U->>FE: pick a Destination Harbor (from GET /web/harbors), Start Journey
    FE->>BE: PUT /web/ships/{id}/shippings {destinationHarbor}
    rect rgb(240,240,240)
    Note over BE,DB: one DB transaction
    BE->>DB: Known Harbor check (else 409, nothing written)
    BE->>BE: Sailors Code from weight and minute
    BE->>DB: quote for Sailors Code
    BE->>DB: update shipping state SHIPPING + destination_harbor
    BE->>DB: insert shipping_outbox row (shipping-published, Origin and Destination Harbor)
    end
    BE-->>FE: Shipping Summary
    CDC->>DB: read WAL (pgoutput)
    CDC->>K: publish to hexagonship-shipping
    K->>T: consume event
    T->>T: print ship, Catain, weight, cargo
```

## 6.4 Arrival at the Destination Harbor (Destination side built)

Per [ADR-0003](../adr/0003-run-each-ship-backend-instance-as-one-harbor.md) and
[ADR-0004](../adr/0004-consume-kafka-events-in-ship-backend-through-an-idempotent-inbox.md). The
Destination side is built by STORY-006 (`ShippingEventListener` → `ArrivalManagementService`); the
Origin side (consuming `ship-arrived`) is planned for STORY-007. Besides the inbox, a ship whose Ship Id
is already in the fleet is skipped, so a re-published Release under a new event id has no effect.

```mermaid
sequenceDiagram
    participant A as Origin Harbor backend
    participant K as Kafka
    participant B as Destination Harbor backend
    participant DBB as Destination PostgreSQL
    A->>K: shipping-published (Origin and Destination Harbor) via outbox
    K->>B: consume (group of Harbor B)
    rect rgb(240,240,240)
    Note over B,DBB: one DB transaction
    B->>DBB: skip if event id already in inbox
    B->>DBB: insert inbox row
    B->>B: ignore unless addressed to this Harbor
    B->>DBB: skip if Ship Id already in fleet
    B->>DBB: Unloading on Arrival - add Cargo to Stock
    B->>DBB: take ship (Ship Id, name, Catain) into fleet
    B->>DBB: insert outbox row (ship-arrived)
    end
    K->>A: ship-arrived
    A->>A: inbox check, Shipping to DONE, remove ship from fleet
```

## 6.5 Harbor startup and discovery (planned)

Per [ADR-0005](../adr/0005-discover-harbors-via-harbor-opened-events-on-a-compacted-topic.md). Not built yet.

```mermaid
sequenceDiagram
    participant H as Harbor backend
    participant DB as PostgreSQL
    participant K as Kafka
    H->>DB: insert outbox row (harbor-opened, Harbor Name)
    DB->>K: Debezium to hexagonship-harbor (compacted)
    K->>H: read hexagonship-harbor from the beginning
    H->>DB: store Known Harbors
```

A Release then offers the Known Harbors other than the current one as Destination Harbor.

> TODO: error scenarios (Debezium down, consumer offline) once the quality scenarios in chapter 10 are set.
