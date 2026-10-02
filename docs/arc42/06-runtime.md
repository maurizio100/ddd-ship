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
    U->>FE: Start Journey
    FE->>BE: PUT /web/ships/{id}/shippings
    rect rgb(240,240,240)
    Note over BE,DB: one DB transaction
    BE->>BE: Sailors Code from weight and minute
    BE->>DB: quote for Sailors Code
    BE->>DB: update shipping state SHIPPING
    BE->>DB: insert shipping_outbox row (shipping-published)
    end
    BE-->>FE: Shipping Summary
    CDC->>DB: read WAL (pgoutput)
    CDC->>K: publish to hexagonship-shipping
    K->>T: consume event
    T->>T: print ship, Catain, weight, cargo
```

> TODO: error scenarios (Debezium down, consumer offline) once the quality scenarios in chapter 10 are set.
