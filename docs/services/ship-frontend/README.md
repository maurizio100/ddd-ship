# ship-frontend

The Hexagonship web UI: the list of Available Ships, creating a ship with its Catain, loading and
unloading Cargo, Releasing a ship, and the Shipping Summary. It holds the UI state in NgRx stores and
gets all its data from ship-backend over REST. It has no domain rules of its own: the backend
decides whether a load or a Release is allowed. The harbor terminal is not part of it.

- **Bounded context(s):** Fleet, CargoLoading, Shipping
- **Building block:** ship-frontend ([05-building-blocks.md](../../arc42/05-building-blocks.md), Level 1)
- **Code lives in:** `ship-frontend/`
- **Tech:** Angular 20 (standalone components), NgRx store + effects, TypeScript 5.9, served by nginx
- **Build & test:** `cd ship-frontend && npm test -- --watch=false --browsers=ChromeHeadless`
- **Talks to:** ship-backend (REST `/web`, including the Catain Images)

## Documents in this folder

Read this index first and then open **only** the documents your task needs.

| Document | What it contains | Read it when |
| --- | --- | --- |
| `guidelines/` | Architecture (layout, routing, components), state and data (NgRx), testing. `guidelines/README.md` indexes them. | Writing or reviewing production or test code in this component |
| `decisions/` | Component-scoped mini-ADRs — decisions binding only this component. `decisions/README.md` indexes them all. | Planning or reviewing a change: scan the index, open the entries that bear on it, and check the change does not contradict one. |
