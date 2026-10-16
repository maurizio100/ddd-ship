# Context: HarborTerminal

The harbor's departure board. It listens for Shipping Published events and announces each ship that
has put to sea, with its Catain, its weight and the Cargo on board. With several Harbors it remains
one departure board for all of them; it is not a Harbor itself.

Terms: [`glossary.md`](glossary.md) — this context's own vocabulary, in the folder beside this file.

## Strategic classification
- **Domain:** Generic — it prints what an event says and holds no rules of its own.
- **Business model:** Engagement — it makes departures visible.
- **Evolution:** Commodity — a plain event consumer.

## Participants
- ship-terminal — a standalone Kafka consumer (group `ship-terminal`) on topic `hexagonship-shipping`.

## Behaviour
- Consume Shipping Published events and print ship name, "Captain Name" (the Catain), weight and the Cargo list (`ship-terminal/src/main/kotlin/Main.kt`).

## Inbound communication
| From | Message | Kind |
|------|---------|------|
| Shipping | Shipping Published | Event |

## Outbound communication
None.

## Business decisions
- The terminal keeps its own copy of the event model and ignores unknown fields; it never shares the backend's classes.
- Events arrive at least once, so the terminal tolerates duplicates.

## Assumptions and open questions
- Open: whether the board should show the Origin Harbor and Destination Harbor that Shipping Published now carries; today it does not.
