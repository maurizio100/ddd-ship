> Status: draft

# 2. Architecture Constraints

## Technical

| Constraint | Background |
|---|---|
| Backend on the JVM: Kotlin 2.2, Java 21, Spring Boot 3.5, Maven | Existing codebase. |
| Frontend: Angular 20 with NgRx, served by nginx | Existing codebase. |
| PostgreSQL 13 as the database, schema managed by Flyway | Required by Debezium's logical replication (`pgoutput`) for the outbox. |
| Containers only: runs via Docker Compose; Kubernetes manifests in `k8s/` | "No development environment needed" to try it out (README). |
| Images built and pushed to Docker Hub by GitHub Actions | `.github/workflows/docker-image.yml`. |

## Organizational

| Constraint | Background |
|---|---|
| Single developer, spare-time project | Keep the moving parts few and the setup reproducible. |
| Work tracked on GitHub (`maurizio100/ddd-ship`), PRs squash-merged to `main` | Existing history. |

## Conventions

- Hexagonal module layout in the backend (`domain`, `driving-adapter`, `driven-adapter`, `application`); the `domain` module has no web or persistence dependencies.
- Ubiquitous language from [`../domain/glossary.md`](../domain/glossary.md), including the "Catain" spelling.
