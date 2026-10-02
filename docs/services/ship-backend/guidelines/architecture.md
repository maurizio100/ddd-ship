# ship-backend — architecture

## Modules and the dependency rule

The hexagonal layers are Maven modules
([ADR-0001](../../../adr/0001-hexagonal-architecture-with-maven-modules.md)), so the build enforces
the dependency rule:

| Module | Contains | May depend on |
|---|---|---|
| `domain` | model, invariants, domain services, driving and driven ports, domain exceptions | `spring-context`, `jakarta.transaction` only: no web, JPA, Jackson or MinIO |
| `driving-adapter` | REST controllers, request/response models, mappers, the exception handler | `domain` |
| `driven-adapter` | JPA entities and repositories, port adapters, outbox writer and event payloads, MinIO client | `domain` |
| `application` | Spring Boot main, configuration, Flyway migrations | all of the above |

Adapters never depend on each other. A controller talks only to driving ports, never to a service
class or a driven adapter.

## Packages

Root package `com.sonicdevelopment`. Packages are split by layer first, then by domain concept
(`ship`, `cargo`, `catain`, `shipping`). They are not split by bounded context: Fleet, CargoLoading
and Shipping share the `Ship` model.

```
domain
  model/            Ship, Shipping, Cargo, Catain (aggregates and entities)
  model/values/     value objects: <Concept>Id, SailorsCode, ShippingQuote, …
  model/enums/      ShippingState
  service/          domain services implementing driving ports
  ports/driving/<concept>/   driving ports + their DTOs
  ports/driven/              driven ports
  exception/        domain rule violations
  converter/        model ↔ DTO conversion
driving.adapter.web
  requestmodel/ responsemodel/ mapper/
driven.adapter
  persistence/<concept>/     entity, Spring Data repository, port adapter
  persistence/outbox/events/ outbox event payloads
  remote/<system>/           clients for remote systems (minio)
```

## Where logic goes

- **Invariants live on the model** (`Ship.addCargo`, `Ship.createNewShipping`), not in services or
  adapters. A service orchestrates: load through a driven port, call the model, save through a
  driven port.
- **Transaction boundaries** are set on domain service methods with `jakarta.transaction.Transactional`.
  If a state change must be published, the outbox row is written in the same transaction (see
  [`persistence.md`](persistence.md)).
- Domain objects never carry JPA annotations; persistence uses separate `*PersistenceEntity` classes.
