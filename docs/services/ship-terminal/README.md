# ship-terminal

The harbor's departure board. A standalone Kafka consumer that reads Shipping Published events from
`hexagonship-shipping` and prints each departed ship with its Catain, weight and Cargo. It filters
records by the `eventType` header and announces only `shipping-published` events; other event types
are skipped. It only reads the published event, keeps its own copy of the event model, and owns no
data. It never calls ship-backend and has no database.

- **Bounded context(s):** HarborTerminal
- **Building block:** ship-terminal ([05-building-blocks.md](../../arc42/05-building-blocks.md), Level 1)
- **Code lives in:** `ship-terminal/`
- **Tech:** Kotlin 2.1 (plain JVM app, no Spring), kafka-clients, Jackson, Maven
- **Build & test:** `cd ship-terminal && mvn test`
- **Talks to:** Kafka (topic `hexagonship-shipping`, group `ship-terminal`)

## Documents in this folder

Read this index first and then open **only** the documents your task needs.

| Document | What it contains | Read it when |
| --- | --- | --- |
| `guidelines/` | Architecture (shape, event contract, delivery, wording) and testing. `guidelines/README.md` indexes them. | Writing or reviewing code in this component |
| `decisions/` | Component-scoped mini-ADRs — decisions binding only this component. `decisions/README.md` indexes them all. | Planning or reviewing a change: scan the index, open the entries that bear on it, and check the change does not contradict one. |
