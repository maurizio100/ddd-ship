# ADR-0009: Carry the Crew with the ship and fill Recruit Pools per Harbor

## Status
Accepted

## Date
2026-10-05

## Context
Ships get a Crew besides their Catain: at its Home Harbor, while a Shipping is being prepared, a User
Hires Recruits from the Harbor's Recruit Pool with one click, paying a Hiring Fee from the Harbor's
Savings, up to the Crew Capacity of the ship's Ship Class. The Crew is meant to make each ship
unique, so it must stay with the ship wherever it sails, and each Harbor should offer different
Recruits. Today a ship crosses Harbors only through Shipping Published (ADR-0003), and the
Destination Harbor rebuilds it from Ship Id, Ship Name and a Catain id it looks up in its own roster;
that works because ADR-0003 relies on the Catain roster and Cargo catalog being seeded identically at
every Harbor. Recruits that differ per Harbor cannot be looked up that way. ADR-0008 already set the
precedent of state (Earnings, Home Harbor) that travels in Shipping Published and is applied in the
inbox transaction of the Arrival.

## Decision
We will carry the ship's Crew in full — each Crew Member's UUID identity, kept at every Harbor, with
its name and attributes — in Shipping Published, and on Arrival store it aboard the ship in the inbox
transaction, replacing any Crew still stored for a returning ship; the Crew never joins the
Destination Harbor's Recruit Pool. Each Harbor fills its own Recruit Pool at runtime, not through
Flyway seeds, so the migrations stay identical at every Harbor. Hiring pays the Hiring Fee from
Savings in the same local transaction that adds the Crew Member, following ADR-0008's money rules.

## Consequences

### Positive
- A Crew sails with its ship to any Harbor, including one that has never seen those Recruits.
- Arrival stays exactly-once: the Crew is written under the same inbox and `arrivals` guards as the
  rest of Arrival (ADR-0004); a Refused Delivery (ADR-0007) takes the Crew home with no extra rule.
- Shipping Published only gains fields; ship-terminal ignores them (R-8) and needs no change.
- Hiring is a purely local transaction: no money or Crew in flight between Harbors.
- Every Harbor still runs the same migrations; only runtime data differs.

### Negative
- The Recruit Pool is the first per-Harbor data that is not identical everywhere, an explicit
  exception to ADR-0003's "seeded identically" consequence; the Catain roster and Cargo catalog keep
  that rule.
- Every Harbor stores copies of foreign Crew Members aboard ships in its fleet, and the event grows
  with the Crew; a returning ship's stored Crew must be replaced, not merged.
- A ship that is lost or stranded takes its Crew with it (as with Earnings, R-13).
- Crew Member data has no shared source: if Recruits get images, they cannot rely on a bucket seeded
  identically at every Harbor (cf. R-15).

### Neutral
- Ship Class values and Crew Capacities, the Hiring Fee amount and whether it is the same at every
  Harbor, and how exactly a pool is filled (generated, configured) are left to the stories.
- Hiring requires the Home Harbor and Savings decided in ADR-0007/ADR-0008 (EPIC-003).

## Alternatives considered
- **An identical seeded Recruit Pool, with only Crew Member ids in the event (the Catain pattern)**:
  Rejected because every Harbor would offer the same Recruits, against the goal of unique Crews.
- **The Crew stays at the Home Harbor and does not sail**: Rejected because the Crew is part of what
  makes the ship unique and must travel with it.
- **A separate crew event or a shared crew registry across Harbors**: Rejected because it adds an
  event type with its own delivery and failure story, and a shared registry contradicts Harbor
  autonomy (ADR-0003), as the shared ledger did in ADR-0008.

## References
- Domain: [Fleet glossary](../domain/contexts/Fleet/glossary.md) (Hire, Recruit, Recruit Pool, Ship Class), [Trade glossary](../domain/contexts/Trade/glossary.md) (Hiring Fee), [shared glossary](../domain/glossary.md) (Crew, Crew Member, Shipping Published), [context map](../domain/context-map.md) (Trade → Fleet)
- arc42: [05 Building Blocks](../arc42/05-building-blocks.md), [06 Runtime](../arc42/06-runtime.md) (6.3, 6.4), [08 Crosscutting](../arc42/08-crosscutting.md) (8.1, 8.2, 8.3), [11 Risks](../arc42/11-risks.md) (R-8, R-13, R-15)
- Related: ADR-0002, ADR-0003, ADR-0004, ADR-0007, ADR-0008
