# Domain

Hexagonship is a playful harbor: ships commanded by cat Catains are loaded with Cargo and Released on
a Shipping, and each departure is announced to a harbor terminal. Each backend instance is a Harbor with
its own fleet, Stock and Savings, and ships sail between Harbors. Derived from the code (no workshop yet).

| Where | What |
|---|---|
| [`context-map.md`](context-map.md) | The authoritative map — every context and the relationships between them. Start here. |
| [`glossary.md`](glossary.md) | Shared terms, the same-word-different-meaning collisions, and an index into every context's own glossary. |
| [`contexts/`](contexts/) | One folder per bounded context: its description and its own glossary. |

## Core flows

- Voyage: [ship created](contexts/Fleet/Fleet.md) → [new Shipping](contexts/Shipping/Shipping.md) → [Cargo loaded](contexts/CargoLoading/CargoLoading.md) → [Release](contexts/Shipping/Shipping.md) → [departure announced](contexts/HarborTerminal/HarborTerminal.md) → [Arrival at the Destination Harbor, Ship Arrived](contexts/Shipping/Shipping.md) → [ship joins its fleet](contexts/Fleet/Fleet.md)
- Delivery: [Incoming Ship](contexts/Shipping/Shipping.md) → [Delivery Price paid from Savings](contexts/Trade/Trade.md) → [Cargo into Stock](contexts/CargoLoading/CargoLoading.md) — or [Refused Delivery, back to the Home Harbor](contexts/Shipping/Shipping.md) → [Earnings credited at the Home Harbor](contexts/Trade/Trade.md)
- Imprisonment (built): [Shipping Published](contexts/Shipping/Shipping.md) → [Contraband counted, Ship Imprisoned](contexts/CoastGuard/CoastGuard.md) → [Confiscation, voyage home](contexts/Shipping/Shipping.md) → [Fine paid from Savings](contexts/Trade/Trade.md) → [ship freed](contexts/Shipping/Shipping.md)

## Bounded contexts

| Context | What it covers | Terms |
|---|---|---|
| [CargoLoading](contexts/CargoLoading/CargoLoading.md) | Loading Cargo onto ships within the Max Weight | [glossary](contexts/CargoLoading/glossary.md) |
| [CoastGuard](contexts/CoastGuard/CoastGuard.md) | Imprisoning ships that carry Contraband too often (counting, Ship Imprisoned, Confiscation, Fine and freeing built) | [glossary](contexts/CoastGuard/glossary.md) |
| [Fleet](contexts/Fleet/Fleet.md) | Ship registry and Catain roster | [glossary](contexts/Fleet/glossary.md) |
| [HarborTerminal](contexts/HarborTerminal/HarborTerminal.md) | Board of every event between the Harbors | [glossary](contexts/HarborTerminal/glossary.md) |
| [Shipping](contexts/Shipping/Shipping.md) | Voyage lifecycle and Release | [glossary](contexts/Shipping/glossary.md) |
| [Trade](contexts/Trade/Trade.md) | A Harbor's money: Prices, Savings, Market, Delivery Price, collecting Earnings and crediting them on Arrival at the Home Harbor (built) | [glossary](contexts/Trade/glossary.md) |
