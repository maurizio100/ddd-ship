# ship-backend — persistence

## Schema and migrations

- **Flyway owns the schema.** Hibernate DDL generation stays off (`ddl-auto: none`).
- Migrations live in `application/src/main/resources/db/migration`, named
  `V<n>__<snake_case_description>.sql` with the next free integer `<n>`.
- Never edit a migration that has been merged; add a new one.
- Reference data (Cargo catalog, Catain roster, Shipping Quotes) is seeded by migrations, not by code.

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
  the aggregate's UUID. `event_type` is kebab-case past tense (`shipping-published`). `payload` is
  the JSON of a `<Name>Event` class in `persistence/outbox/events`.
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
- It is not in the Debezium connector's `table.include.list` and is never published.
- Binary data (Catain Images) goes to MinIO, never into the database.
