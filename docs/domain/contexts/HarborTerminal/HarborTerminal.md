# Context: HarborTerminal

The board of every event between the Harbors. It announces each ship that has put to sea, with its
Catain, its weight and the Cargo on board; each ship that has arrived; each Harbor that has opened;
and each ship the Coast Guard has imprisoned. With several Harbors it remains one board for all of
them; it is not a Harbor itself.

Terms: [`glossary.md`](glossary.md) — this context's own vocabulary, in the folder beside this file.

## Strategic classification
- **Domain:** Generic — it prints what an event says and holds no rules of its own.
- **Business model:** Engagement — it makes what happens between the Harbors visible.
- **Evolution:** Commodity — a plain event consumer.

## Participants
- ship-terminal — a standalone Kafka consumer (group `ship-terminal`) on topics `hexagonship-shipping`, `hexagonship-harbor` and `hexagonship-coast-guard`.

## Behaviour
- Announce a Shipping Published as a departure: ship name, Catain, weight (no unit) and the Cargo list.
- Announce a Ship Arrived as an arrival: the ship, its Destination Harbor and its Origin Harbor.
- Announce a Harbor Opened: the Harbor Name.
- Announce a Ship Imprisoned: the ship, the Harbor holding it and its Home Harbor.
- Show an Unknown Event as one line with its kind and its content as received.

## Inbound communication
| From | Message | Kind |
|------|---------|------|
| Shipping | Shipping Published | Event |
| Shipping | Ship Arrived | Event |
| Shipping | Harbor Opened | Event |
| CoastGuard | Ship Imprisoned | Event |

## Outbound communication
None.

## Business decisions
- The terminal keeps its own copy of the event model and ignores unknown fields; it never shares the backend's classes.
- Events arrive at least once, so the terminal tolerates duplicates.
- An event that cannot be read is reported in one line and skipped; neither it nor an Unknown Event stops the board.

## Assumptions and open questions
- Open: whether the board should show the Origin Harbor and Destination Harbor that Shipping Published now carries; today it does not.
