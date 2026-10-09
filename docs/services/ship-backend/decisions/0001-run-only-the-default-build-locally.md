# 0001: Run only the default build locally; leave -Pdb to CI

- **Status:** Accepted
- **Date:** 2026-10-09
- **Component:** ship-backend

## Context
ADR-0010 made `./mvnw verify` Docker-free and moved the Postgres tests to the `-Pdb` profile, which
CI runs on every pull request. Contributors, including coding agents, still ran `-Pdb` locally
before every push. Each run starts Postgres containers, takes minutes and produces long logs, which
undoes most of what ADR-0010 saved.

## Decision
Locally we run only the default build, narrowed to the module being worked on, and `-Pdb` runs in
CI. We run `-Pdb` locally only when a change touches the persistence adapters, a Flyway migration or
a transaction boundary, and then only for the affected module.

## Consequences
- The inner loop needs no Docker and stays short and quiet:
  - while working: `./mvnw -q verify -pl <module> -am`;
  - once before pushing: `./mvnw -q verify`;
  - always with `JAVA_HOME` on JDK 21.
- Persistence changes still get a local check: `./mvnw -q verify -Pdb -pl driven-adapter -am`, or
  `-pl application -am` for migrations and transaction tests.
- A change that breaks only a `-Pdb` test is first reported by CI, one push later. The pull request
  goes back to its author instead of failing locally.
- The rule depends on the author judging correctly whether a change touches persistence. A miss
  costs a CI round-trip, not a defect on main, because CI runs `-Pdb` before merge.

## Alternatives considered
- **Run `-Pdb` locally before every push**: what agents did until now. Rejected because it costs
  minutes and thousands of log lines per story for a check CI repeats anyway.
- **Never run `-Pdb` locally**: rejected because persistence and migration changes are exactly the
  ones `-Pdb` exists for, and a local run there is cheaper than a CI round-trip.

## References
- [ADR-0010](../../../adr/0010-test-each-component-in-isolation.md)
- [Testing guideline](../guidelines/testing.md)
