# How to run two Harbors locally

Two Harbors ("Tortuga" and "Port Royal") run side by side, each with its own frontend, backend,
PostgreSQL and MinIO, sharing one Kafka and Kafka Connect. Layout: one Compose project per Harbor,
from the single parameterised `docker-compose-harbor.yml` ([ADR-0003](../../adr/0003-run-each-ship-backend-instance-as-one-harbor.md),
[arc42 7.4](../../arc42/07-deployment.md)).

## Ports

| Harbor | Env file | Frontend | Backend | PostgreSQL | MinIO API / console |
|---|---|---|---|---|---|
| Tortuga | `harbors/tortuga.env` | 80 | 8080 | 5432 | 9000 / 9001 |
| Port Royal | `harbors/port-royal.env` | 81 | 8081 | 5433 | 9100 / 9101 |

Ports are env-configurable in the env file. Stop any other Hexagonship Compose stack first: they
use the same host ports and `hexagonship-*` names.

## Start

Run from the repo root. Build the backend and frontend images the first time (the `up` below does it).

1. Start Kafka and Kafka Connect. This also creates the shared network `my_kafka_network`.
   ```bash
   docker compose -f docker-compose-kafka.yml up -d
   ```
2. Create the compacted topic `hexagonship-harbor` once, before the first Harbor starts
   ([ADR-0005](../../adr/0005-discover-harbors-via-harbor-opened-events-on-a-compacted-topic.md)).
   It is idempotent.
   ```bash
   kafka-connect/connect-helpers/create-harbor-topic
   ```
3. Start each Harbor as its own project:
   ```bash
   docker compose -p tortuga    --env-file harbors/tortuga.env    -f docker-compose-harbor.yml up -d --build
   docker compose -p port-royal --env-file harbors/port-royal.env -f docker-compose-harbor.yml up -d --build
   ```
4. Register one outbox connector per Harbor (Kafka Connect on `localhost:8083`):
   ```bash
   kafka-connect/connect-helpers/upload-connector kafka-connect/connectors/shipping-outbox-tortuga.json
   kafka-connect/connect-helpers/upload-connector kafka-connect/connectors/shipping-outbox-port-royal.json
   ```
   Both publish to `hexagonship-shipping`; each has its own connector name, `database.hostname`
   (`<slug>-db-postgres`) and replication slot.
   They set `publication.autocreate.mode` to `filtered`, so the `dbz_publication` Debezium creates
   covers only `public.shipping_outbox`.
   To add another Harbor, copy an env file in `harbors/` and a connector in `kafka-connect/connectors/`
   with its own slug, ports, connector name, `database.hostname` and `slot.name`. Keep
   `"database.port": "5432"`: Kafka Connect reaches the database inside the Docker network, not on
   the env file's `POSTGRES_PORT`.

## Expected result

- Each backend logs its consumer group `ship-backend-<Harbor Name>` (`ship-backend-Tortuga`, `ship-backend-Port Royal`); both groups show in Kafka UI (`localhost:8005`).
- `hexagonship-harbor` shows `cleanup.policy=compact` in Kafka UI.
- A Release in either Harbor produces a message on `hexagonship-shipping`.

## Fix a database whose publication covers all tables

A Harbor database whose connector was registered before the outbox connectors set
`publication.autocreate.mode` has a `dbz_publication` created `FOR ALL TABLES`. With `filtered`,
Debezium cannot narrow such a publication, so recreate it by hand before uploading the updated
connector. `<slug>` is `tortuga` or `port-royal`.

1. Check the publication. If `puballtables` shows `t`, run the steps below.
   ```bash
   docker exec <slug>-db-postgres psql -U hexagonship_user -d hexagonship -c "SELECT pubname, puballtables FROM pg_publication;"
   ```
2. Stop writes, so no change falls between dropping and recreating the publication:
   ```bash
   docker stop <slug>-backend
   ```
3. Delete the connector. Its replication slot and offsets survive.
   ```bash
   kafka-connect/connect-helpers/delete-connector shipping-outbox-<slug>
   ```
4. Recreate the publication in one transaction:
   ```bash
   docker exec <slug>-db-postgres psql -U hexagonship_user -d hexagonship -c "BEGIN; DROP PUBLICATION dbz_publication; CREATE PUBLICATION dbz_publication FOR TABLE public.shipping_outbox; COMMIT;"
   ```
5. Upload the updated connector and start the backend again:
   ```bash
   kafka-connect/connect-helpers/upload-connector kafka-connect/connectors/shipping-outbox-<slug>.json
   docker start <slug>-backend
   ```
6. Verify that only the outbox is published, and that the connector is `RUNNING`:
   ```bash
   docker exec <slug>-db-postgres psql -U hexagonship_user -d hexagonship -c "SELECT * FROM pg_publication_tables;"
   kafka-connect/connect-helpers/get-connector-status shipping-outbox-<slug>
   ```

The same steps apply to the single-Harbor stack: container `hexagonship-db-postgres`, connector
`shipping-outbox` (`kafka-connect/connectors/ship-outbox-connector.json`), and its backend container
`hexagonship-backend` (stop and start it with `docker stop hexagonship-backend` / `docker start hexagonship-backend`). A `ships_cargos` set to `REPLICA IDENTITY FULL` by hand needs nothing more:
Flyway's `V12__ships_cargos_primary_key.sql` gives it a primary key and sets its replica identity back to it.

## Frontend to backend

The Angular production build calls the relative path `/web`. Each frontend proxies this to its own
Harbor's backend via nginx, configured with the `BACKEND_URL` environment variable. The frontend
port (80 or 81) gives you the full working UI.

## Stop

Kafka stays up; stop Harbors independently:

```bash
docker compose -p tortuga    down      # add -v to also wipe this Harbor's data volumes
docker compose -p port-royal down
docker compose -f docker-compose-kafka.yml down   # when finished with Kafka
```

Deleting a Harbor's connector: `kafka-connect/connect-helpers/delete-connector shipping-outbox-<slug>`.
