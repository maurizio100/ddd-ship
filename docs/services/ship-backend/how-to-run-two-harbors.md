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

## Expected result

- Each backend logs its consumer group `ship-backend-<Harbor Name>` (`ship-backend-Tortuga`, `ship-backend-Port Royal`); both groups show in Kafka UI (`localhost:8005`).
- `hexagonship-harbor` shows `cleanup.policy=compact` in Kafka UI.
- A Release in either Harbor produces a message on `hexagonship-shipping`.

## Frontend to backend

The Angular production build calls the relative path `/web`, but the frontend image's `nginx.conf`
does not proxy it (Kubernetes does that via the Ingress). The Compose setups therefore do not give
a working UI on the frontend port alone; use the backend directly on its port (`http://localhost:8080/web`,
`http://localhost:8081/web`). Adding an nginx proxy is a frontend change outside this story.

## Stop

Kafka stays up; stop Harbors independently:

```bash
docker compose -p tortuga    down      # add -v to also wipe this Harbor's data volumes
docker compose -p port-royal down
docker compose -f docker-compose-kafka.yml down   # when finished with Kafka
```

Deleting a Harbor's connector: `kafka-connect/connect-helpers/delete-connector shipping-outbox-<slug>`.
