> Status: draft

# 10. Quality Requirements

> **All response measures below are proposals** derived from the quality goals in chapter 1 and the
> current code. The project owner sets the actual targets; until then treat the numbers as
> placeholders, not commitments.

## Quality tree

| Quality goal (ch. 1) | Scenarios |
|---|---|
| Exemplary structure | QS-1, QS-2 |
| Reliable event publication | QS-3, QS-4 |
| Easy to run | QS-5 |
| Changeability | QS-6 |

## Scenarios

| ID | Stimulus | Environment | Response | Response measure (proposed) |
|---|---|---|---|---|
| QS-1 | A developer adds a dependency on Spring Web / JPA / MinIO to the `domain` module | Normal build | The build rejects it | Build fails; 0 framework I/O imports in `domain` (needs an ArchUnit or Maven enforcer rule — TODO) |
| QS-2 | A new reader wants to find where a rule (e.g. Max Weight) is enforced | Repo checkout | The rule is in the domain model, named in the glossary | Found in ≤ 1 hop from the glossary term to one class in `domain` |
| QS-3 | The backend releases a Shipping while Kafka Connect is down | Compose with Kafka | The Release succeeds; the event is published once Connect is back | 0 lost events; published ≤ 60 s after Connect restarts |
| QS-4 | The DB transaction of a Release fails after the state change | Any | Neither the state change nor the event is persisted | 0 events published for rolled-back Releases |
| QS-5 | A newcomer with only Docker installed wants to try the app | Fresh machine | One command starts the full app | ≤ 1 command, ≤ 5 min until the ships list loads |
| QS-6 | A new driven adapter (e.g. a different image store) replaces MinIO | Development | Only the adapter and wiring change | 0 changes in `domain` and `driving-adapter` |
