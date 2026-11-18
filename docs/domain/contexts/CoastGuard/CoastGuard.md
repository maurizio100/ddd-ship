# Context: CoastGuard

The Coast Guard watches the seas between all Harbors. It reads every Shipping Published, counts per
ship how often it carries Contraband, and imprisons a ship that does so too often by publishing Ship
Imprisoned. Like HarborTerminal it serves all Harbors and is not a Harbor itself. Planned
(coast-guard ideation, 2026-11-18): nothing of it is built yet.

Terms: [`glossary.md`](glossary.md) — this context's own vocabulary, in the folder beside this file.

## Strategic classification
- **Domain:** Supporting — a playful rule on top of the core Shipping flow.
- **Business model:** Engagement — it makes the harbor game more fun.
- **Evolution:** Genesis — a new experiment, in a technology new to the project.

## Participants
- The Coast Guard — a component of its own beside the Harbors, keeping its own copy of the event model (planned).

## Behaviour
Not built yet.

## Inbound communication
| From | Message | Kind |
|------|---------|------|
| Shipping | Shipping Published | Event |

## Outbound communication
| To | Message | Kind |
|----|---------|------|
| Shipping | Ship Imprisoned | Event |

## Business decisions
- A ship is judged by its Ship Id, across every Harbor it sails from: the Coast Guard counts the voyages (Shipping Published) carrying Contraband.
- Decided 2026-11-18: the third voyage carrying Contraband within 10 minutes imprisons the ship; the Coast Guard publishes Ship Imprisoned, naming the ship, the Harbor holding it (the Destination Harbor of the voyage that tipped the count) and the ship's Home Harbor, so the Home Harbor learns that its ship is imprisoned.
- Decided 2026-11-18: once a ship is imprisoned its count starts again at zero.
- Decided 2026-11-18: each voyage counts once, by its Shipping id, however often its Shipping Published arrives (events arrive at least once, and a Harbor may publish the same Shipping again).
- The Coast Guard judges ships after their Arrival: it never holds a voyage back, so an Arrival never waits for it.
