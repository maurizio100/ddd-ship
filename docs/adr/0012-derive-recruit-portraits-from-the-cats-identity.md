# ADR-0012: Derive Recruit Portraits from the cat's identity

## Status
Accepted

## Date
2026-11-20

## Context
Recruits and Crew Members need a portrait, and a Crew Member must look the same at every Harbor it
sails to. ADR-0009 noted that Crew Member data has no shared source, so Recruit images cannot rely
on a bucket seeded identically at every Harbor (R-15), the way Catain Images are shipped with the
backend. No cat artwork exists for Recruits. A Crew Member keeps the UUID of the Recruit it was
Hired as, and that UUID already travels with the ship in Shipping Published (ADR-0009).

## Decision
We will derive a Recruit Portrait from the cat's UUID alone and never store or transmit it. The
backend domain value object `Portrait.of(uuid)` maps the UUID's bits to a fur colour, a coat
pattern, an eye colour and an accessory; the REST responses for Recruits and Crew Members carry the
result, and the frontend draws it as an inline SVG (`app-cat-portrait`) in the theme's colours.

## Consequences

### Positive
- A Crew Member shows the same portrait at every Harbor by construction, with no new event field,
  no migration and no image store.
- Recruits generated before portraits existed have portraits too.
- The frontend holds no rule: it only draws the attributes it is given.

### Negative
- `Portrait.of` and its enum value sets are a contract between every Harbor and the frontend:
  changing the derivation or reordering a value set changes the look of every existing cat.
- A Harbor running older code serves no portrait for its cats.
- The portraits are drawn shapes, not artwork; richer images would need a new decision.

### Neutral
- The value sets (6 fur colours, 4 coat patterns, 3 eye colours, 4 accessories) can grow only by
  appending, and appending still changes cats whose bits map onto the new values.

## Alternatives considered
- **Image files in a bucket or shipped with the backend (the Catain pattern)**: Rejected because
  Recruits differ per Harbor and no artwork exists (ADR-0009, R-15).
- **A portrait key chosen when the Recruit is generated, stored and carried in Shipping
  Published**: Rejected because the UUID already identifies the cat at every Harbor; storing a key
  needs a migration, a backfill for existing Recruits and Crew Members, and an event field with a
  fallback for older Harbors.
- **Deriving the portrait in the frontend**: Rejected because the look is domain data the backend
  acceptance tests assert, and the frontend keeps no rules.

## References
- Domain: [Fleet glossary](../domain/contexts/Fleet/glossary.md) (Recruit Portrait, Recruit, Crew Member)
- arc42: [11 Risks](../arc42/11-risks.md) (R-15)
- Related: ADR-0003, ADR-0009
