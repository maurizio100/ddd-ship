> Status: usable

# 9. Architecture Decisions

Index of the architecture decision records in [`../adr/`](../adr/index.md). Maintained by `adr-writing`.

- [ADR-0001: Structure ship-backend as a hexagon of Maven modules](../adr/0001-hexagonal-architecture-with-maven-modules.md) — Accepted — structure · ship-backend
- [ADR-0002: Publish domain events via a transactional outbox and Debezium](../adr/0002-transactional-outbox-via-debezium.md) — Accepted — integration · Shipping
- [ADR-0003: Run each ship-backend instance as one Harbor](../adr/0003-run-each-ship-backend-instance-as-one-harbor.md) — Accepted — structure · ship-backend
- [ADR-0004: Consume Kafka events in ship-backend through an idempotent inbox](../adr/0004-consume-kafka-events-in-ship-backend-through-an-idempotent-inbox.md) — Accepted — integration · ship-backend
- [ADR-0005: Discover Harbors via harbor-opened events on a compacted topic](../adr/0005-discover-harbors-via-harbor-opened-events-on-a-compacted-topic.md) — Accepted — integration · Shipping
- [ADR-0006: Push fleet changes to the frontend with Server-Sent Events](../adr/0006-push-fleet-changes-to-the-frontend-with-server-sent-events.md) — Accepted — integration · ship-backend, ship-frontend
- [ADR-0007: Unload Incoming Ships manually after Arrival](../adr/0007-unload-incoming-ships-manually-after-arrival.md) — Accepted — integration · ship-backend, ship-frontend
- [ADR-0008: Carry Earnings home with the ship](../adr/0008-carry-earnings-home-with-the-ship.md) — Accepted — integration · ship-backend
