# Services

Per-component implementation conventions — coding style, test conventions, and anything
else that is code-adjacent and too volatile for the architecture chapters.

Each component has a folder under `docs/services/` fronted by a `README.md` that states the
component's purpose and indexes that folder's documents. Start there: read the component's
`README.md` and let its index tell you which document to open next.

Use the `component-docs` skill to add or revise a component's documents — it keeps the index
in step.

## Components

- [ship-backend](ship-backend/README.md) — REST API, domain logic, persistence and outbox for Fleet, CargoLoading and Shipping
- [ship-frontend](ship-frontend/README.md) — Angular/NgRx UI for managing ships, loading Cargo and Releasing
- [ship-terminal](ship-terminal/README.md) — Kafka consumer that announces departed ships (HarborTerminal)
