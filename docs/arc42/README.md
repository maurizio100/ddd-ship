# Architecture (arc42)

Architecture documentation for Hexagonship, following the
[arc42](https://arc42.org/) template. Each chapter is a file in this section; the
sidebar lists them in order.

The table below is the index of this section. Skills and agents read it to decide which
chapter to open, so it says what each chapter covers **in this project**, not what arc42
puts there in general. The `arc42-authoring` skill keeps it in step with the chapters.

## Chapters

| Chapter | Covers | Status |
|---|---|---|
| [1. Introduction and Goals](01-introduction.md) | What Hexagonship is for, its quality goals and stakeholders | draft |
| [2. Constraints](02-constraints.md) | Technical, organizational and convention constraints | draft |
| [3. Context and Scope](03-context.md) | The Harbor's business and technical context, the events exchanged with other Harbors, and the foreign systems (PostgreSQL, MinIO, Kafka) | draft |
| [4. Solution Strategy](04-solution-strategy.md) | The key strategies (hexagonal modules, transactional outbox, idempotent inbox, manual unloading, money travelling with the ship) and the goals they serve | draft |
| [5. Building Blocks](05-building-blocks.md) | The components and the hexagonal Maven modules of ship-backend | draft |
| [6. Runtime](06-runtime.md) | Scenarios step by step: create, load, Release, Arrival, Harbor discovery, fleet push, unload or refuse, Earnings, Hiring | draft |
| [7. Deployment](07-deployment.md) | Docker Compose, Kubernetes, running several Harbors, and the build pipeline | draft |
| [8. Crosscutting Concepts](08-crosscutting.md) | Domain language, persistence, transactions and event publication, errors, configuration, logging, API conventions, test strategy (accepted, ADR-0010) | draft |
| [9. Decisions](09-decisions.md) | Index of the ADRs | usable |
| [10. Quality Requirements](10-quality.md) | Quality tree and scenarios, with proposed measures | draft |
| [11. Risks and Technical Debt](11-risks.md) | Known risks and debt, and the high-risk areas a change must not skim | draft |
| [12. Glossary](12-glossary.md) | Points to the domain glossary and overview in `docs/domain/` | stable |
