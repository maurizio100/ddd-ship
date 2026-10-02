> Status: stable

# 12. Glossary

The domain vocabulary is maintained in the domain model, not here:

- [Shared glossary and index](../domain/glossary.md) — shared terms and links to every context's own glossary.
- [Domain overview](../domain/README.md) — bounded contexts and the context map.

Technical terms specific to this documentation:

| Term | Meaning |
|---|---|
| Driving port / adapter | Inbound side of the hexagon: interfaces the domain offers (`*ManagementPort`, `*InformationPort`) and the REST controllers that call them. |
| Driven port / adapter | Outbound side: interfaces the domain needs (`*RepositoryPort`, `CatainImageRemotePort`, `ShippingOutboxRepository`) and their JPA / MinIO / outbox implementations. |
| Outbox | The `shipping_outbox` table; rows are events waiting to be streamed to Kafka by Debezium. |
