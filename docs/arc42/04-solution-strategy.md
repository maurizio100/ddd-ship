> Status: draft

# 4. Solution Strategy

| Choice | Justification (goal / constraint) |
|---|---|
| **Hexagonal Architecture** in the backend: a framework-free `domain` module with driving and driven ports, adapters in separate Maven modules, wired in `application`. | Goal 1 (exemplary structure) and 4 (changeability): the module boundaries enforce the dependency rule at build time. |
| **DDD-style domain model** with value-object ids (`ShipId`, `CargoId`, …) and invariants on the model (`Ship.addCargo`, `Ship.createNewShipping`). | Goal 1; the [domain model](../domain/README.md) is the reference for the language. |
| **Transactional Outbox + Debezium**: Release writes the shipping and an outbox row in one DB transaction; Debezium streams the outbox to Kafka. | Goal 2 (reliable publication) without two-phase commit or polling code. |
| **Each backend instance is one Harbor**; ships move between Harbors only through events. | Goal 2 and the project's aim of trying out event-driven integration; every Harbor is the same artifact. |
| **Idempotent inbox** for events the backend consumes from Kafka (driving adapter). | Goal 2: at-least-once delivery must not change a Harbor twice — today the Stock, once EPIC-003 is built an Incoming Ship and a credit of Earnings. |
| **Harbor discovery** via `harbor-opened` events on a compacted topic. | Goal 4: a new Harbor needs no change to the others. |
| **Spring Boot + Kotlin, PostgreSQL + Flyway** | Constraint 2; Flyway versions the schema and seeds the reference data (cargo, Catains, quotes). |
| **Angular SPA with NgRx** served by nginx, talking REST to `/web` | Existing frontend; NgRx is itself a concept being tried out (#205). |
| **Angular Material + CDK with one pirate theme** (decided, EPIC-002) | Goal 4: screens are assembled from one component library and theme instead of hand-written CSS ([ship-frontend decision 0001](../services/ship-frontend/decisions/0001-build-the-ui-on-angular-material-with-a-pirate-theme.md)). |
| **Arriving Cargo waits aboard for a User decision** (decided, EPIC-003): an arriving ship joins the fleet as an Incoming Ship; unloading and paying, or refusing, are User actions. | Goal 2 and 4: the protocol between Harbors stays unchanged and no event handler depends on a Harbor's Savings ([ADR-0007](../adr/0007-unload-incoming-ships-manually-after-arrival.md)). |
| **Money travels with the ship** as Earnings and is kept as exact decimals (decided, EPIC-003). | Goal 2: money only changes inside local transactions, never in flight between Harbors ([ADR-0008](../adr/0008-carry-earnings-home-with-the-ship.md)). |
| **Object storage (MinIO) for images** | Keeps binaries out of the database. |
| **Containers everywhere**: Docker Compose for local, Kubernetes manifests for cluster runs | Goal 3 (easy to run) and constraint "no toolchain needed". |

Decisions: [ADR-0001](../adr/0001-hexagonal-architecture-with-maven-modules.md) (hexagonal module structure), [ADR-0002](../adr/0002-transactional-outbox-via-debezium.md) (transactional outbox via Debezium), [ADR-0003](../adr/0003-run-each-ship-backend-instance-as-one-harbor.md) (one Harbor per backend instance), [ADR-0004](../adr/0004-consume-kafka-events-in-ship-backend-through-an-idempotent-inbox.md) (idempotent inbox), [ADR-0005](../adr/0005-discover-harbors-via-harbor-opened-events-on-a-compacted-topic.md) (Harbor discovery), [ADR-0006](../adr/0006-push-fleet-changes-to-the-frontend-with-server-sent-events.md) (fleet push), [ADR-0007](../adr/0007-unload-incoming-ships-manually-after-arrival.md) (manual unloading), [ADR-0008](../adr/0008-carry-earnings-home-with-the-ship.md) (Earnings and money).
