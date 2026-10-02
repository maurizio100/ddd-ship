# ADR-0001: Structure ship-backend as a hexagon of Maven modules

## Status
Proposed

## Date
2026-10-02

## Context
Hexagonship exists to try out Hexagonal Architecture and DDD on a small domain (README); the code is
meant to be a readable reference, and the project is expected to keep growing. Ports-and-adapters can
be expressed by package convention alone, but conventions erode silently in a codebase without
reviewers or tests. *Recorded retroactively: the structure has been in place since early in the
project; the rationale is inferred from the README and the code, not from a written discussion.*

## Decision
We structure ship-backend as four Maven modules: `domain` (model, driving and driven ports, domain
services — no web, persistence or storage dependencies), `driving-adapter` (REST), `driven-adapter`
(JPA, outbox, MinIO) and `application` (Spring Boot wiring, Flyway). Dependencies point only towards
`domain`.

## Consequences

### Positive
- The dependency rule is enforced by the build: `domain` cannot import JPA or Spring Web because they are not on its classpath.
- Adapters can be replaced (e.g. another image store) without touching `domain`.
- The structure itself demonstrates the pattern, which is the project's goal.

### Negative
- More ceremony for small features: a change typically touches port, service, adapter, DTOs and mappers across modules.
- Mapping layers (domain ↔ DTO ↔ request/response ↔ persistence entity) duplicate shapes.
- `domain` still depends on `spring-context` (`@Service`) and `jakarta.transaction` (`@Transactional`), so it is framework-light, not framework-free.

### Neutral
- Modules are split by technical role, not by bounded context; Fleet, CargoLoading and Shipping share one `domain` module and the `Ship` model.

## Alternatives considered
- **Layered architecture in a single module** (controller → service → repository). Rejected because it does not demonstrate ports and adapters and lets persistence concerns leak into the model.
- **Hexagon by package convention in one module.** Rejected because nothing stops a violation; the Maven split makes the boundary a compile-time fact.
- **One module per bounded context.** Not taken; the domain is small and the contexts share the `Ship` model today.

## References
- arc42: [04 Solution Strategy](../arc42/04-solution-strategy.md), [05 Building Blocks — Level 2](../arc42/05-building-blocks.md)
- Domain: [context map](../domain/context-map.md) (Shared Kernel between backend contexts)
