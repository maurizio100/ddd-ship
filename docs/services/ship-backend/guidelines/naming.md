# ship-backend — naming

Names come from the ubiquitous language in [`docs/domain/`](../../../domain/README.md), including
the spelling "Catain". `<Concept>` below is a domain term: `Ship`, `Cargo`, `Catain`, `Shipping`.

| Kind | Pattern | Example |
|---|---|---|
| Driving port, commands | `<Concept>ManagementPort` | `ShipManagementPort`, `CargoLoadManagementPort` |
| Driving port, queries | `<Concept>InformationPort` | `ShipInformationPort` |
| Domain service | the port name with `Service` in place of `Port` | `ShipManagementService` |
| Driving-port DTO | `<Concept>DTO`, `<Concept>DetailDTO`, `<Concept>CreationDataDTO`, `<Concept>UpdateDataDTO` | `ShipDetailDTO` |
| Driven port, persistence | `<Concept>RepositoryPort` | `ShipRepositoryPort` |
| Driven port, remote system | `<Concept><System>RemotePort` or `<Concept>RemotePort` | `CatainImageRemotePort` |
| Identity value object | `<Concept>Id` wrapping a `UUID` | `ShipId` |
| Domain exception | a rule-violation phrase + `Exception` | `ShipTooHeavyException` |
| Controller | `<Concept>Controller`; ship sub-resources: `Ship<Sub>Controller` | `ShipCargoController` |
| HTTP models | `<X>Request` / `<X>Response` | `CargoLoadRequest`, `ShipDetailResponse` |
| Web mapper | top-level `to<X>Response(…)` functions in `mapper/` | `toShipDetailResponse` |
| JPA entity | `<Concept>PersistenceEntity` | `ShipPersistenceEntity` |
| Spring Data repository | `<Concept>PersistenceEntityRepository` | `ShipPersistenceEntityRepository` |
| Port adapter | `<Concept>RepositoryAdapter` | `ShipRepositoryAdapter` |
| Outbox payload | `<Name>Event` | `ShippingEvent` |
| Inbound event copy | `<Name>InboundEvent` | `ShippingPublishedInboundEvent` |
| Kafka listener | `<Concept>EventListener` | `ShippingEventListener` |
| Inbox port / adapter | `InboxRepositoryPort` / `InboxRepositoryAdapter` | |

Some existing driven ports don't follow the pattern yet (`CatainRepository`, `CargoPersistencePort`,
`CargoQueryPort`, `ShippingOutboxRepository`). New ports follow the table. Rename an existing one
only in a chore story, not as a side effect of other work.
