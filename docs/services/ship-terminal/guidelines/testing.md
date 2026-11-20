# ship-terminal — testing

**Run:** `cd ship-terminal && mvn test`

JUnit 5 + `kotlin-test`, in `src/test/kotlin`. No Kafka broker is needed for any test.

| What | How |
|---|---|
| Record handling | `handleRecord()` announces `shipping-published`, `ship-arrived`, `harbor-opened` and `ship-imprisoned`. A record of an unknown kind (or with no `eventType` header) is shown as one line with its kind and its content as received; a known kind whose value cannot be read is reported in one line. Neither stops the loop. Tests in `RecordFilterTest.kt` and `EventAnnouncementTest.kt`. |

- Build records with the public `ConsumerRecord` constructor that takes `Headers`; give a skipped
  record a non-null value, so the test fails when the `eventType` check is removed.
- Not covered yet: deserializer and formatter tests.

- Payload samples live in `src/test/resources/events/<event-type>[-<variant>].json`, e.g.
  `shipping-published.json`, `ship-arrived.json`, `harbor-opened.json`, `ship-imprisoned.json`.
  They are copies of real payloads as the terminal receives them: a JSON string, as Debezium
  delivers outbox payloads and as the Coast Guard writes `ship-imprisoned`.
- When ship-backend adds a field to the event, add a sample with that field here in the same story.
