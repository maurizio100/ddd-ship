# ship-backend — persistence

## Schema and migrations

- **Flyway owns the schema.** Hibernate DDL generation stays off (`ddl-auto: none`).
- Migrations live in `application/src/main/resources/db/migration`, named
  `V<n>__<snake_case_description>.sql` with the next free integer `<n>`.
- Never edit a migration that has been merged; add a new one.
- Reference data (Cargo catalog, Catain roster, Shipping Quotes) is seeded by migrations, not by code.
- The business ids of Cargo and Catains (`cargo_id`, `catain_id`) are fixed literal UUIDs, identical at
  every Harbor (`V9__same_reference_ids_at_every_harbor.sql`), and unique (`uq_cargos_cargo_id`,
  `uq_catains_catain_id`): an arriving ship names its Catain and Loaded Cargo by id. Never seed them
  with `gen_random_uuid()` or any other random value.

## Tables and columns

- Table names are plural `snake_case` (`ships`, `shippings`, `cargos`); join tables combine both
  names (`ships_cargos`).
- Every entity table (not the outbox or inbox, see below) has a `BIGINT` surrogate primary key `id`, taken from a sequence
  `<table>_seq` (`INCREMENT BY 50`). The UUID business id goes in a separate column `<entity>_id`
  (`ship_id`, `cargo_id`).
- Columns are `snake_case` with the entity as prefix (`cargo_name`, `cargo_weight`).
- A foreign key column is also named `<target>_id`, but it is a `BIGINT` referencing the target's
  surrogate `id`. So `ship_id` is the UUID in `ships` and a BIGINT foreign key in `shippings`:
  always check the column type.
- `ships_cargos` holds a ship's Loaded Cargo. Despite its name, its `ship_id` column references
  `shippings(id)`.
- Constraints are named `pk_<table>` and `fk_<table>_on_<target>`.

## JPA mapping

- JPA lives only in `driven-adapter`. Each aggregate has a `*PersistenceEntity`, and the port
  adapter maps between the entity and the domain model; domain objects are never annotated.
- The outside world identifies a row by its business id. The surrogate `id` never leaves
  `driven-adapter`.

## Outbox

- Any state change that has to be published writes a row to `shipping_outbox` through a driven port,
  **in the same transaction** as the change ([ADR-0002](../../../adr/0002-transactional-outbox-via-debezium.md)).
- Row fields: `aggregate_type` sets the topic (`hexagonship-<aggregate_type>`). `aggregate_id` is
  the aggregate's UUID and becomes the Kafka key. `event_type` is kebab-case past tense
  (`shipping-published`). `payload` is the JSON of a `<Name>Event` class in
  `persistence/outbox/events`.
- `payload` is a plain `String` on `ShippingOutboxPersistenceEntity`. Each outbox adapter serializes
  its own `<Name>Event` with a plain `ObjectMapper()`, so the published JSON does not depend on the
  application's Jackson configuration.
- The table is shared by all outbox events despite its name. Harbor events use
  `aggregate_type = 'harbor'` (topic `hexagonship-harbor`, compacted), and their `aggregate_id` is the
  UUIDv5 of the Harbor Name in the fixed namespace `d5a17a2e-8e74-5937-8c7d-101e395ae650`. Every
  Harbor must use the same namespace, so a Harbor keeps one key across restarts and compaction
  keeps one record per Harbor.
- `ship-arrived` uses `aggregate_type = 'shipping'` (topic `hexagonship-shipping`) and the Shipping id
  as `aggregate_id`, the same key as the `shipping-published` it answers, so both stay ordered per Shipping.
- Changing an event payload changes a public contract: only add fields, and never rename or remove one.

## Inbox

- `inbox_events` holds the ids of events consumed from other Harbors
  ([ADR-0004](../../../adr/0004-consume-kafka-events-in-ship-backend-through-an-idempotent-inbox.md)).
  The event id is the Debezium `id` header, i.e. the publishing Harbor's outbox `message_id`.
- It is keyed by `event_id` and, like `shipping_outbox`, has no surrogate `id` or sequence: the
  primary key on the event id *is* the idempotency guarantee.
- It is written only through `InboxRepositoryPort`, which runs in the caller's transaction
  (`Propagation.MANDATORY`) and records with `INSERT … ON CONFLICT DO NOTHING`, so the check and the
  record are one statement.
- It also records a Harbor's own events that Debezium relays back to it (its own `harbor-opened`),
  since every Harbor consumes the topics it publishes to.
- It is not in the Debezium connector's `table.include.list` and is never published.

## Known Harbors

- `known_harbors` holds the Harbor Names this Harbor has learned of, with a surrogate `id` from
  `known_harbors_seq`. `uq_known_harbors_harbor_name` makes each Harbor Name appear once.
- It is written only through `KnownHarborRepositoryPort.rememberHarbor`, in the caller's transaction
  (`Propagation.MANDATORY`) with `INSERT … ON CONFLICT (harbor_name) DO NOTHING`, together with the
  inbox record of the event. A Harbor's own name is never stored.
- It is not in the Debezium connector's `table.include.list` and is never published.

## Stock

- `stocks` holds this Harbor's Stock: one row per Cargo, with a surrogate `id` from `stocks_seq`,
  `cargo_id` referencing `cargos(id)` (`uq_stocks_cargo_id`) and `stock_quantity`.
- The Starting Stock is seeded by a migration (`V7__stocks.sql`), which Flyway applies once per
  database, i.e. when the Harbor opens for the first time. Don't seed it from code or check for an
  empty table.
- It is written only through `StockRepositoryPort`, in the caller's transaction
  (`Propagation.MANDATORY`), so a Stock change commits or rolls back with the cargo load.
- Taking from Stock is one conditional statement (`UPDATE … SET stock_quantity = stock_quantity - 1
  WHERE … AND stock_quantity > 0`); zero updated rows means out of Stock. Never read the quantity,
  change it in memory and save it back: concurrent loads would lose updates.
  `ck_stocks_stock_quantity_not_negative` backs the "never below 0" rule up.
- Putting into Stock is `INSERT … ON CONFLICT (cargo_id) DO UPDATE`, so a Cargo without a row gets one.
- It is not in the Debezium connector's `table.include.list` and is never published.

## Binary data

- Binary data (Catain Images) goes to MinIO, never into the database.
