# ship-terminal — architecture

## Shape

A plain Kotlin console application (no Spring), using `kafka-clients` and Jackson. It subscribes to
`hexagonship-shipping` as consumer group `ship-terminal` and prints one announcement per Shipping
Published event.

Keep three concerns apart, so each can be tested without Kafka:

| Concern | What it is |
|---|---|
| Event model + deserializer | the terminal's own copy of the event and a Kafka `Deserializer` for it |
| Formatting | a pure function `format…(event): String` that builds the announcement text |
| Consume loop | `main`: config, subscribe, poll, print the formatted text |

## The event contract

- The terminal **keeps its own copy** of the event model. It never depends on ship-backend classes:
  it is a Conformist on the published event (see the [context map](../../../domain/context-map.md)).
- **Tolerant reader:** unknown properties are ignored (`FAIL_ON_UNKNOWN_PROPERTIES = false`).
  A field the backend added later is nullable or has a default, so older events still parse.
- Field names follow the backend payload exactly (`shipEventData`, `shippingEventData`, `catain`).
  Debezium wraps the payload as a JSON string, so the deserializer unwraps it once before mapping.
- An event that cannot be parsed is logged and skipped. It never stops the loop.

## Delivery

Events arrive at least once. A duplicate is announced twice, which is acceptable for a departure
board, so the terminal keeps no dedup store.

The terminal filters records by the `eventType` header (set by Debezium EventRouter). Only
`shipping-published` events are announced; other event types (e.g. `ship-arrived`) are skipped with
a debug log. Unknown event types never stop the poll loop.

## Wording

The output uses the domain terms, with the ship's Catain shown under the "Captain Name" label (an
alias recorded in the HarborTerminal glossary). New output text uses the canonical terms from
[`docs/domain/`](../../../domain/README.md).
