# ship-terminal — testing

**Run:** `cd ship-terminal && mvn test`

JUnit 5 + `kotlin-test`, in `src/test/kotlin`. No Kafka broker is needed for any test.

| What | How |
|---|---|
| Record filter | `handleRecord()` returns `true` only for a `shipping-published` record with a parsed event; `false` for a missing `eventType` header, another event type (e.g. `ship-arrived`) and a `shipping-published` record without a readable value (logged as a warning). Test in `RecordFilterTest.kt`. |

- Build records with the public `ConsumerRecord` constructor that takes `Headers`; give a skipped
  record a non-null value, so the test fails when the `eventType` check is removed.
- Not covered yet: deserializer and formatter tests.

- Payload samples live in `src/test/resources/events/<event-type>[-<variant>].json`, e.g.
  `shipping-published.json`, `ship-arrived.json`. They are copies of real
  outbox payloads as Debezium delivers them (a JSON string).
- When ship-backend adds a field to the event, add a sample with that field here in the same story.
