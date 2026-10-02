---
# https://vitepress.dev/reference/default-theme-home-page
layout: home

hero:
  name: "Hexagonship"
  text: "Documentation"
  tagline: "Pet project for trying out Hexagonal Architecture, Domain-Driven Design and the Transactional Outbox pattern with Kafka Connect/Debezium"
  actions:
    - theme: brand
      text: Architecture
      link: /arc42/
    - theme: alt
      text: Domain model
      link: /domain/

features:
  - title: Architecture (arc42)
    details: System design, quality goals, building blocks, runtime and crosscutting concerns.
    link: /arc42/
    linkText: Read the architecture
  - title: Decisions (ADR)
    details: The architectural decisions that shaped the system, and why.
    link: /adr/
    linkText: Browse decisions
  - title: Domain
    details: The ubiquitous language, bounded contexts, and the context map.
    link: /domain/
    linkText: Explore the domain
  - title: Services
    details: Per-component coding and test conventions.
    link: /services/
    linkText: See conventions
---
