# Domain

Hexagonship is a playful harbor: ships commanded by cat Catains are loaded with Cargo and Released on
a Shipping, and each departure is announced to a harbor terminal. Derived from the code (no workshop yet).
Planned: several Harbors, each with its own fleet and Stock, between which ships sail (harbor-voyages
ideation, 2026-10-03; marked "Planned" in the context files).

| Where | What |
|---|---|
| [`context-map.md`](context-map.md) | The authoritative map — every context and the relationships between them. Start here. |
| [`glossary.md`](glossary.md) | Shared terms, the same-word-different-meaning collisions, and an index into every context's own glossary. |
| [`contexts/`](contexts/) | One folder per bounded context: its description and its own glossary. |

## Bounded contexts

| Context | What it covers | Terms |
|---|---|---|
| [CargoLoading](contexts/CargoLoading/CargoLoading.md) | Loading Cargo onto ships within the Max Weight | [glossary](contexts/CargoLoading/glossary.md) |
| [Fleet](contexts/Fleet/Fleet.md) | Ship registry and Catain roster | [glossary](contexts/Fleet/glossary.md) |
| [HarborTerminal](contexts/HarborTerminal/HarborTerminal.md) | Departure board consuming Shipping Published | [glossary](contexts/HarborTerminal/glossary.md) |
| [Shipping](contexts/Shipping/Shipping.md) | Voyage lifecycle and Release | [glossary](contexts/Shipping/glossary.md) |
| [Trade](contexts/Trade/Trade.md) | A Harbor's money: Prices, Savings, Market, Delivery Price (built); Earnings (planned) | [glossary](contexts/Trade/glossary.md) |
