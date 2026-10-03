> Status: draft

# 5. Building Block View

## Level 1 — whitebox Hexagonship

```mermaid
flowchart LR
    FE["ship-frontend<br/>Angular + NgRx"]
    BE["ship-backend<br/>Spring Boot, hexagonal"]
    DB[("PostgreSQL")]
    S3[("MinIO")]
    CDC["Kafka Connect<br/>Debezium outbox router"]
    K[["Kafka"]]
    TERM["ship-terminal<br/>Kafka consumer"]
    FE -->|"REST /web"| BE
    BE --> DB
    BE --> S3
    DB -->|"CDC shipping_outbox"| CDC --> K --> TERM
```

| Block | Responsibility | Interface | Bounded contexts |
|---|---|---|---|
| ship-frontend | UI: ships list, create ship, cargo loading, release, Shipping Summary. State in NgRx stores (`ships`, `catains`). | Consumes REST `/web/*` | Fleet, CargoLoading, Shipping (UI) |
| ship-backend | All domain logic and persistence; writes the outbox. | REST `/web/ships`, `/web/ships/{id}/cargos`, `/web/ships/{id}/shippings`, `/web/cargos`, `/web/catains` (`ship-backend/openapi.yml`) | Fleet, CargoLoading, Shipping |
| Kafka Connect (Debezium) | Turns outbox rows into events on `hexagonship-<aggregate_type>`. | Connector config in `kafka-connect/connectors/` | — (infrastructure) |
| ship-terminal | Prints each departed ship. | Consumes `hexagonship-shipping` | HarborTerminal |

### Planned: Harbors

Per [ADR-0003](../adr/0003-run-each-ship-backend-instance-as-one-harbor.md), the whole block set
above (frontend, backend, PostgreSQL, MinIO, Debezium connector) is deployed once per Harbor, and the
Harbors share one Kafka. ship-backend then also **consumes** events
([ADR-0004](../adr/0004-consume-kafka-events-in-ship-backend-through-an-idempotent-inbox.md)).
ship-terminal stays one departure board for all Harbors. Not built yet.

```mermaid
flowchart LR
    subgraph HA["Harbor A"]
        BEA["ship-backend"]
        DBA[("PostgreSQL")]
    end
    subgraph HB["Harbor B"]
        BEB["ship-backend"]
        DBB[("PostgreSQL")]
    end
    K[["Kafka"]]
    TERM["ship-terminal"]
    BEA --> DBA -->|"CDC outbox"| K
    BEB --> DBB -->|"CDC outbox"| K
    K -->|"consume"| BEA
    K -->|"consume"| BEB
    K --> TERM
```

## Level 2 — ship-backend

Module structure per [ADR-0001](../adr/0001-hexagonal-architecture-with-maven-modules.md).

```mermaid
flowchart LR
    subgraph driving["driving-adapter"]
        WEB["REST controllers"]
    end
    subgraph domain["domain (no framework I/O)"]
        IN["driving ports"]
        SVC["domain services"]
        MDL["model: Ship, Shipping, Cargo, Catain"]
        OUT["driven ports"]
    end
    subgraph driven["driven-adapter"]
        JPA["JPA persistence adapters"]
        OBX["outbox adapter"]
        MIN["MinIO adapter"]
    end
    APP["application<br/>Spring Boot main, Flyway"]
    WEB --> IN --> SVC --> MDL
    SVC --> OUT
    JPA -.implements.-> OUT
    OBX -.implements.-> OUT
    MIN -.implements.-> OUT
    APP --- driving & domain & driven
```

| Block | Responsibility |
|---|---|
| `domain` | Model and invariants, driving ports (`*ManagementPort`, `*InformationPort`) and driven ports (`*RepositoryPort`, `ShippingOutboxRepository`, `CatainImageRemotePort`). Depends only on `spring-context` and `jakarta.transaction`. |
| `driving-adapter` | Maps HTTP requests/responses to driving ports. |
| `driven-adapter` | Implements driven ports: JPA entities and repositories, the outbox writer (`ShippingEvent` payload), the MinIO client. |
| `application` | Boot entry point, configuration, Flyway migrations (`db/migration`). |

Planned ([ADR-0004](../adr/0004-consume-kafka-events-in-ship-backend-through-an-idempotent-inbox.md)):
`driving-adapter` gains a **Kafka listener** (spring-kafka) that maps inbound events to the driving
ports, and `driven-adapter` gains an **inbox adapter** that records consumed event ids and a store for
the Known Harbors. Stock is persisted next to the Cargo catalog.

The backend has no package-level split by bounded context yet; Fleet, CargoLoading and Shipping all
share the `Ship` model (Shared Kernel in the [context map](../domain/context-map.md)).
