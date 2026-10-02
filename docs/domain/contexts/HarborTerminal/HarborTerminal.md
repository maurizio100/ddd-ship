# Context: HarborTerminal

The harbor's departure board. It listens for Shipping Published events and announces each ship that
has put to sea, with its Catain, its weight and the Cargo on board.

Terms: [`glossary.md`](glossary.md) — this context's own vocabulary, in the folder beside this file.

## Participants
- ship-terminal — a standalone Kafka consumer (group `ship-terminal`) on topic `hexagonship-shipping`.

## Behaviour
- Consume Shipping Published events and print ship name, "Captain Name" (the Catain), weight and the Cargo list (`ship-terminal/src/main/kotlin/Main.kt`).
- Keeps its own copy of the event model; unknown fields are ignored.

## Tactical model

> Status: not yet discovered
> The tactical model emerges during implementation. Each story that
> touches this context adds or modifies entries below via the
> `implement-story` skill. Do not pre-populate from the discovery
> source — tactical choices are made at code time, not at analysis time.

### Aggregates
_No entries yet._

### Entities (non-aggregate roots)
_No entries yet._

### Value objects
_No entries yet._

### Domain services
_No entries yet._
