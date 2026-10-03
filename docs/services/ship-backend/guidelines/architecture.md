# ship-backend — architecture

## Modules and the dependency rule

The hexagonal layers are Maven modules
([ADR-0001](../../../adr/0001-hexagonal-architecture-with-maven-modules.md)), so the build enforces
the dependency rule:

| Module | Contains | May depend on |
|---|---|---|
| `domain` | model, invariants, domain services, driving and driven ports, domain exceptions | `spring-context`, `jakarta.transaction` only: no web, JPA, Jackson or MinIO |
| `driving-adapter` | REST controllers, request/response models, mappers, the exception handler; Kafka listeners and inbound event copies | `domain` |
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
driving.adapter.messaging    Kafka listeners, InboundMessageReader
  events/                    inbound event copies (<Name>InboundEvent)
driven.adapter
  persistence/<concept>/     entity, Spring Data repository, port adapter
  persistence/inbox/         inbox of consumed event ids
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

## Consuming events

- A Kafka listener only maps and delegates: it reads the record with `InboundMessageReader`, maps the
  payload onto its inbound copy and calls a driving port with the `EventId`. It never touches the
  inbox or a driven port.
- Inbound copies (`messaging/events/`) are the backend's own classes, never the outbox payloads from
  `driven.adapter.persistence.outbox.events`, and they ignore unknown fields.
- The domain service that handles an event takes its `EventId` and, inside its transactional method,
  calls `InboxRepositoryPort.recordConsumedEvent` first. If that returns `false` the event was already
  consumed, and the service returns without any effect. The inbox row and the state change commit
  together ([ADR-0004](../../../adr/0004-consume-kafka-events-in-ship-backend-through-an-idempotent-inbox.md)).
- Every consumed event is inbox-recorded this way, including `harbor-opened`. Where the same fact can
  arrive under a new event id (a Harbor that opens again writes a new outbox row), the effect is
  additionally idempotent on its business key: Known Harbors are stored once per Harbor Name.
- A `@KafkaListener` that has an `id` (needed to start or stop it through
  `KafkaListenerEndpointRegistry`) must set `idIsGroup = false`. Otherwise the id replaces the
  Harbor's consumer group.
- The backend must start with Kafka unreachable. Where no Kafka runs (app-only Compose, k8s),
  `SPRING_KAFKA_LISTENER_AUTO_STARTUP=false` keeps the listeners idle. Don't add anything that blocks
  startup on the broker, such as `NewTopic` beans or a missing-topics check. Work done on startup
  (opening the Harbor) only writes to the outbox and never talks to Kafka.
- The consumer group is `ship-backend-<Harbor Name>`, derived from `harbor.name` (`HARBOR_NAME`),
  which every instance must set
  ([ADR-0003](../../../adr/0003-run-each-ship-backend-instance-as-one-harbor.md)).
