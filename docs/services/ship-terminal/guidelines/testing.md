# ship-terminal — testing

**Run:** `cd ship-terminal && mvn test`

JUnit 5 + `kotlin-test`, in `src/test/kotlin`. No Kafka broker is needed for any test.

| What | How |
|---|---|
| Deserializer | Feed it the bytes of a recorded event and assert on the parsed `ShippingEvent`. Include a payload with an extra unknown field, and one that doesn't parse (expect `null`, no exception). |
| Formatter | Build an event and assert on the exact announcement text, including the "No cargo loaded." case. |

- Payload samples live in `src/test/resources/events/<event-type>[-<variant>].json`, e.g.
  `shipping-published.json`, `shipping-published-unknown-field.json`. They are copies of real
  outbox payloads as Debezium delivers them (a JSON string).
- When ship-backend adds a field to the event, add a sample with that field here in the same story.
