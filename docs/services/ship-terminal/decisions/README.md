# ship-terminal — decisions

Component-scoped decisions for `ship-terminal`, as **mini-ADRs**. Each file records one decision
that binds **only this component**: context, the decision, consequences, alternatives.

**What belongs here vs. `docs/adr/`.** A decision binding **more than one** component, or setting a
project-wide precedent, is a global ADR in `docs/adr/` and is indexed in
`docs/arc42/09-decisions.md`. A decision binding **exactly this** component lives here — a
single-component project does not push its decisions to `docs/adr/`. If a decision changes what a
first-class arc42 chapter says about the system's current state, it is not component-scoped.

**How entries are created.** Via the `adr-writing` skill (its component-scoped mode) — triggered by
`implement-story`'s learnings gate, or invoked directly. Not by hand and not by the implementor.
IDs are per-component: this folder has its own `0001`, `0002`, … sequence. `adr-writing` adds the
index row below in the same change as the decision file.

## Index

<One row per decision, sorted by ID ascending. `adr-writing` maintains this — read it first and
open only the decisions that bear on your task.>

| ID | Decision | Status | Category | Date |
| --- | --- | --- | --- | --- |
| — | _No decisions recorded yet._ | | | |
