# ship-coast-guard

The Coast Guard. A standalone Kafka Streams app that reads Shipping Published events from
`hexagonship-shipping`, counts per ship the voyages carrying Contraband, and publishes Ship Imprisoned
(`ship-imprisoned`) to `hexagonship-coast-guard` when a ship carries Contraband three times within 10
minutes ([ADR-0011](../../adr/0011-imprison-contraband-runners-with-a-coast-guard-on-kafka-streams.md)).
It keeps its own copy of the event model and its counts in a Kafka Streams state store; it never calls
ship-backend and has no database. Decided, not built yet: the code does not exist (EPIC-005).

- **Bounded context(s):** CoastGuard
- **Building block:** ship-coast-guard ([05-building-blocks.md](../../arc42/05-building-blocks.md), Level 1)
- **Code lives in:** `ship-coast-guard/` (not created yet)
- **Tech:** Kotlin, Kafka Streams, Maven (planned)
- **Build & test:** to be written by the first story, with `TopologyTestDriver` tests needing no broker ([08-crosscutting.md](../../arc42/08-crosscutting.md), 8.8)
- **Talks to:** Kafka (reads `hexagonship-shipping`, writes `hexagonship-coast-guard`)

## Documents in this folder

Read this index first and then open **only** the documents your task needs.

| Document | What it contains | Read it when |
| --- | --- | --- |
| `decisions/` | Component-scoped mini-ADRs — decisions binding only this component. `decisions/README.md` indexes them all. | Planning or reviewing a change: scan the index, open the entries that bear on it, and check the change does not contradict one. |
