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
- Field names follow the published payload exactly (`shipEventData`, `shippingEventData`, `catain`).
  The value arrives as a JSON string (Debezium for outbox events, the Coast Guard for
  `ship-imprisoned`), so it is unwrapped once before mapping; a plain JSON object is accepted too.
- An event that cannot be parsed is logged and skipped. It never stops the loop.

## Delivery

Events arrive at least once. A duplicate is announced twice, which is acceptable for the board,
so the terminal keeps no dedup store. `hexagonship-harbor` is compacted, so a terminal without
committed offsets announces every Harbor Opened again on start.

The terminal reads the `eventType` header first and parses the value only for the kind it names:
`shipping-published`, `ship-arrived`, `harbor-opened` or `ship-imprisoned`. Any other kind is an
Unknown Event, shown as one line with its kind and its content as received. Neither an Unknown Event
nor an unreadable event stops the poll loop.

## Wording

The output uses the domain terms, with the ship's Catain shown under the "Captain Name" label (an
alias recorded in the HarborTerminal glossary). New output text uses the canonical terms from
[`docs/domain/`](../../../domain/README.md).

A departure announcement ends with the Crew block, before the `===` separator: `🐾 Crew:` and one
numbered line per Crew Member, `<name> (<trait>)`, or `🐾 Sails without Crew.` when
`shipEventData.crew` is absent or empty.
