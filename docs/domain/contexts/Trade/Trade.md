# Context: Trade

A Harbor's money. Every Cargo has a Price; a Harbor holds Savings, pays the Delivery Price for the
Cargo ships bring it, buys Cargo at the Market, and receives the Earnings its own ships bring home.
Candidate context from the harbor-economy ideation (2026-10-04). Built so far: Price and Savings
(STORY-024), the Market (STORY-025), paying the Delivery Price on Unload (STORY-045) and collecting it as the
ship's Earnings; crediting the Earnings a ship carries home on Arrival is planned.

Terms: [`glossary.md`](glossary.md) — this context's own vocabulary, in the folder beside this file.

## Participants
- User — manages the Harbor on the harbor management page: buys Cargo at the Market and unloads or refuses Incoming Ships.

## Behaviour
- When a Harbor opens, it rolls a Price for every catalog Cargo that has none yet (a whole-dollar amount from 30.00 $ to 60.00 $) and keeps it: opening again changes no Price. The Prices and the Harbor Opened outbox row are written in one transaction. *Introduced by STORY-024.*
- A Harbor holds Savings, starting at 1000.00 $ when its database is first migrated; a restart keeps them. The harbor management page shows the Savings and each Cargo's Price (`GET /web/savings`, `GET /web/stock`). *Introduced by STORY-024.*
- The User buys a quantity of a Cargo at the Market at its Price (`POST /web/market/purchases`). The Harbor pays Price × quantity from its Savings and puts the Cargo into its Stock, in one transaction. A purchase the Savings cannot cover is refused whole ("The Savings do not cover 100.00 $"), and so is a Cargo that has no Price yet. Bought Cargo is ordinary Stock. *Introduced by STORY-025.*
- The User unloads an Incoming Ship (`POST /web/incoming-ships/{id}/unloading`). The Harbor pays its Delivery Price from its Savings and puts every Cargo aboard into its Stock, in one transaction; the ship stops being Incoming. In the same transaction the ship earns the Delivery Price: away from its Home Harbor it is added to the ship's Earnings, at its Home Harbor it goes back into the Savings at once and the ship's Earnings stay as they are. An unloading the Savings cannot cover is refused whole ("The Savings do not cover the Delivery Price of 115.00 $"), and so is one with a Cargo aboard that has no Price yet.
- A ship's Earnings travel with it in Shipping Published and are restored on Arrival at the next Harbor; the fleet card shows them when the ship carries any.
- Not built: crediting the Earnings a ship carries to its Home Harbor's Savings when it arrives there (ADR-0008).

## Tactical model

> Status: not yet discovered
> The tactical model emerges during implementation. A story adds an
> entry here only for a new aggregate, a changed aggregate boundary or
> a new invariant — the code is the rest of the model. Do not
> pre-populate from the discovery source: tactical choices are made at
> code time, not at analysis time.

### Aggregates
_No entries yet._

### Invariants
- **Savings** — never drop below 0. A payment the Savings cannot cover — a Market purchase or a Delivery Price — is refused whole and changes neither the Savings nor what it was paying for; paying is one conditional update (`SavingsRepositoryPort.pay`). A Delivery Price is paid at most once per unloading. Crediting the Savings is one atomic increment (`SavingsRepositoryPort.receive`).
- **Ship (Shared Kernel) and Savings** — a ship's Earnings never drop below 0 and only grow by a Delivery Price paid away from its Home Harbor, as one atomic increment (`ShipRepositoryPort.addEarnings`). At the Home Harbor the Delivery Price paid goes back into the Savings in the same unloading transaction. Both writes come before the unloading's conditional clear, so an unloading that loses to a concurrent one rolls them back with its payment.

### Entities (non-aggregate roots)
_No entries yet._

### Value objects
_No entries yet._

### Domain services
_No entries yet._
