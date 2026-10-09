# Context: Trade

A Harbor's money. Every Cargo has a Price; a Harbor holds Savings, pays the Delivery Price for the
Cargo ships bring it, buys Cargo at the Market, and receives the Earnings its own ships bring home.
Candidate context from the harbor-economy ideation (2026-10-04). Built so far: Price and Savings
(STORY-024); Market, Delivery Price and Earnings are planned.

Terms: [`glossary.md`](glossary.md) — this context's own vocabulary, in the folder beside this file.

## Participants
- User — manages the Harbor on the harbor management page: buys Cargo at the Market and unloads or refuses Incoming Ships.

## Behaviour
- When a Harbor opens, it rolls a Price for every catalog Cargo that has none yet (a whole-dollar amount from 30.00 $ to 60.00 $) and keeps it: opening again changes no Price. The Prices and the Harbor Opened outbox row are written in one transaction. *Introduced by STORY-024.*
- A Harbor holds Savings, starting at 1000.00 $ when its database is first migrated; a restart keeps them. The harbor management page shows the Savings and each Cargo's Price (`GET /web/savings`, `GET /web/stock`). *Introduced by STORY-024.*
- Not built: the Market, the Delivery Price and the Earnings, described by the harbor-economy epic and its ADRs.

## Tactical model

> Status: not yet discovered
> The tactical model emerges during implementation. A story adds an
> entry here only for a new aggregate, a changed aggregate boundary or
> a new invariant — the code is the rest of the model. Do not
> pre-populate from the discovery source: tactical choices are made at
> code time, not at analysis time.

### Aggregates
_No entries yet._

### Entities (non-aggregate roots)
_No entries yet._

### Value objects
_No entries yet._

### Domain services
_No entries yet._
