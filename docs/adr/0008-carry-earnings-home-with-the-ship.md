# ADR-0008: Carry Earnings home with the ship

## Status
Accepted

## Date
2026-10-04

## Context
The harbor economy gives every Cargo a Price and every Harbor Savings (Starting Savings 1000 $,
never below 0). A Destination Harbor pays the Delivery Price when it unloads an Incoming Ship
(ADR-0007), and that money belongs to the ship's Home Harbor, which may be neither the Origin nor
the Destination of the voyage. Each Harbor owns its data and Harbors talk only through events
(ADR-0003); consumed events are handled once through the inbox (ADR-0004). Money is new to the
project: the only numeric domain value so far, Weight, is a Float, and arc42 has no convention for
amounts.

## Decision
We will keep the Delivery Price paid on Unload with the ship as its Earnings, carry the Earnings and
the Home Harbor in Shipping Published, and credit the Earnings to the Home Harbor's Savings in the
inbox transaction of the Arrival there, with no separate payment event and no shared ledger. Money is
dollars to two decimals: an exact decimal in the domain, `NUMERIC(…, 2)` in PostgreSQL and a decimal
string in event JSON, never a binary float.

## Consequences

### Positive
- Money only changes inside a local transaction (Unload, Market purchase, Arrival at the Home
  Harbor); there is never a payment in flight to reconcile between Harbors.
- Crediting Earnings is exactly-once by the same inbox and `arrivals` guards as the rest of Arrival.
- Shipping Published only gains fields; ship-terminal ignores them (R-8) and needs no change.
- Amounts add up exactly; no rounding drift between Harbors.

### Negative
- Earnings are only as safe as the ship: a ship that never reaches its Home Harbor, or sails to a
  Harbor that never answers, keeps its Earnings out of every Savings.
- The Home Harbor sees no money until the ship comes home, possibly after several voyages.
- The ship's state now includes Earnings, which every Harbor must keep while the ship is in its
  fleet and hand on in Shipping Published.
- Money and Weight follow different numeric conventions in the same model.

### Neutral
- A ship at its Home Harbor that is paid a Delivery Price there has its Earnings credited at once.
- Prices are per Harbor: each Harbor rolls a whole-dollar Price between 30.00 $ and 60.00 $ for every
  Cargo once, when it first opens, and keeps it (changed 2026-10-07, STORY-024; originally identical
  at every Harbor). A Delivery Price uses the paying Harbor's Prices, so Earnings are worth what that
  Harbor paid. Prices are not announced to other Harbors; doing so would need its own decision.

## Alternatives considered
- **A settlement event from the paying Harbor straight to the Home Harbor**: Rejected because it adds
  an event type and its own delivery and failure story, and the domain wants the money to come home
  with the ship.
- **A central ledger or market service shared by all Harbors**: Rejected because each Harbor owns its
  data and talks to others only through events (ADR-0003); one service would couple every Harbor's
  availability.
- **Float or double amounts, like Weight**: Rejected because binary floats cannot hold cents exactly
  and sums would drift.

## References
- Domain: [Trade glossary](../domain/contexts/Trade/glossary.md), [shared glossary](../domain/glossary.md) (Home Harbor, Shipping Published), [Shipping](../domain/contexts/Shipping/Shipping.md) (Harbor that never answers)
- arc42: [03 Context](../arc42/03-context.md), [06 Runtime](../arc42/06-runtime.md) (6.4), [08 Crosscutting](../arc42/08-crosscutting.md) (8.2, 8.3), [11 Risks](../arc42/11-risks.md) (R-8, R-10)
- Related: ADR-0002, ADR-0003, ADR-0004, ADR-0005, ADR-0007
