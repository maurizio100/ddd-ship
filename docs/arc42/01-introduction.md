> Status: draft

# 1. Introduction and Goals

Hexagonship is a pet project for **trying out architecture concepts on a small, playful domain**: a
harbor where ships commanded by cat Catains are loaded with Cargo and Released on a Shipping, and
each departure is announced to a harbor terminal (see [`../domain/`](../domain/README.md)).
The domain is deliberately simple so that the architecture is the interesting part.

## Requirements overview

- Register ships with a name and a Catain; rename and remove them.
- Load and unload Cargo onto a ship within its Max Weight.
- Start a Shipping for a ship and Release it; show the Shipping Summary.
- Publish every Release as an event that other systems (the harbor terminal) consume.

## Quality goals

| Priority | Goal | Why |
|---|---|---|
| 1 | **Exemplary structure** — the code is a clean, readable reference for Hexagonal Architecture and DDD. | It is a learning/showcase project; the structure is the product. |
| 2 | **Reliable event publication** — a Release is never lost or published without being stored. | Demonstrates the Transactional Outbox pattern. |
| 3 | **Easy to run** — the whole system starts with Docker Compose, with no local toolchain needed. | Others should be able to try it out (README). |
| 4 | **Changeability** — new concepts (e.g. Kubernetes, NgRx) can be added without reworking the core. | "This project will grow and grow." |

> TODO: confirm priorities with the project owner — derived from the README and code, not stated anywhere.

## Stakeholders

| Role | Expectations |
|---|---|
| Project owner / developer (maurizio100) | A playground for concepts; code stays understandable as it grows. |
| Readers / learners | Can start the system easily and follow how the patterns are applied. |
