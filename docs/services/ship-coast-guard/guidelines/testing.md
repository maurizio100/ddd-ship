# ship-coast-guard — testing

No Kafka broker is needed for any test.

| What | How |
|---|---|
| Gherkin scenarios | One `TopologyTestDriver` test per scenario in `CoastGuardAcceptanceTest`. It pipes raw records onto the input topic with their `eventType` header and an explicit record timestamp, and reads what the topology writes to the output topic. |
| Counting rules | Unit tests on `ShipWatch`, which holds the rules and no Kafka types: the 10-minute window, once per Shipping id, the reset after an imprisonment, one count per ship. |

- Time in a test comes from the record timestamps it pipes, never from the wall clock.
