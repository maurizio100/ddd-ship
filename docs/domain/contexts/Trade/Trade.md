# Context: Trade

A Harbor's money. Every Cargo has a Price; a Harbor holds Savings, pays the Delivery Price for the
Cargo ships bring it, buys Cargo at the Market, and receives the Earnings its own ships bring home.
A Delivery Price paid away from a ship's Home Harbor becomes the ship's Earnings, which it carries
from Harbor to Harbor until it reaches home.

Terms: [`glossary.md`](glossary.md) — this context's own vocabulary, in the folder beside this file.

## Strategic classification
- **Domain:** Core — the harbor economy is what makes Releasing ships worth it, and it carries the trickiest rules (money in an at-least-once consumer).
- **Business model:** Engagement — earning and spending gives the User a goal.
- **Evolution:** Genesis — the newest context, still being shaped by EPIC-003.

## Participants
- User — manages the Harbor on the harbor management page: buys Cargo at the Market and unloads or refuses Incoming Ships.

## Behaviour
- When a Harbor opens, it rolls a Price for every catalog Cargo that has none yet (a whole-dollar amount from 30.00 $ to 60.00 $). The Prices and the Harbor Opened outbox row are written in one transaction.
- A Harbor holds Savings, starting at 1000.00 $ when its database is first migrated; a restart keeps them. The harbor management page shows the Savings and each Cargo's Price (`GET /web/savings`, `GET /web/stock`).
- The User buys a quantity of a Cargo at the Market at its Price (`POST /web/market/purchases`). The Harbor pays Price × quantity from its Savings and puts the Cargo into its Stock, in one transaction. Bought Cargo is ordinary Stock.
- The User unloads an Incoming Ship (`POST /web/incoming-ships/{id}/unloading`). The Harbor pays its Delivery Price from its Savings and puts every Cargo aboard into its Stock, in one transaction; the ship stops being Incoming. In the same transaction the ship earns the Delivery Price: away from its Home Harbor it is added to the ship's Earnings, at its Home Harbor it goes back into the Savings at once and the ship's Earnings stay as they are.
- A ship's Earnings travel with it in Shipping Published and are restored on Arrival at the next Harbor; the fleet card shows them when the ship carries any.
- When a ship arrives at its Home Harbor, the Earnings it carries go into the Savings and the ship carries none, in the Arrival's inbox transaction. At any other Harbor the ship keeps its Earnings.

## Inbound communication
| From | Message | Kind |
|------|---------|------|
| Shipping | Pay the Delivery Price of an unloaded Incoming Ship | Command |
| Shipping | Credit the Earnings of a ship arriving at its Home Harbor | Command |

## Outbound communication
| To | Message | Kind |
|----|---------|------|
| CargoLoading | Put Cargo into the Stock (bought at the Market, or unloaded from an Incoming Ship) | Command |

## Business decisions
- A Cargo's Price is rolled once, when the Harbor first opens without one; opening again changes no Price.
- Money is kept in dollars ($), as exact decimals to two places.
- The Savings never drop below 0. A payment the Savings cannot cover, a Market purchase or a Delivery Price, is refused whole and changes neither the Savings nor what it was paying for ("The Savings do not cover 100.00 $", "The Savings do not cover the Delivery Price of 115.00 $").
- Cargo that has no Price yet can be neither bought nor delivered: the purchase or the unloading is refused.
- A Delivery Price is paid at most once per unloading. When two unloadings of the same ship race, the losing one rolls back its payment, its Stock writes and its Earnings or credit.
- A ship's Earnings never drop below 0 and grow only by a Delivery Price paid away from its Home Harbor. At the Home Harbor the Delivery Price goes back into the Savings in the same unloading.
- An Arrival at the ship's Home Harbor hands all the ship's Earnings to the Savings and resets them to 0.00 $, in the Arrival's inbox transaction and behind its inbox and `arrivals` guards, so each Arrival credits at most once.

## Assumptions and open questions
- Trade is still a candidate context (harbor-economy ideation, 2026-10-04); its boundary has not been confirmed.
- Earnings of a ship that never reaches its Home Harbor stay out of every Savings (accepted, `docs/arc42/11-risks.md` R-13).
