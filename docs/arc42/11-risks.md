> Status: draft

# 11. Risks and Technical Debt

Observed in the code during the initial documentation pass (2026-10-02). These are **findings, not
decisions** — each needs a call from the project owner (fix, accept, or turn into a story).

| # | Type | Finding | Where | Impact |
|---|---|---|---|---|
| R-1 | Debt / possible bug | Nothing ever sets a Shipping to `DONE`, so a ship that has been Released can never get a new Shipping. | `Ship.createNewShipping`, `ShippingState` | Resolved by STORY-007: the Origin Harbor sets the Shipping to `DONE` when it consumes `ship-arrived` (6.4). |
| R-2 | Possible bug | The Sailors Code is `mod 14` (0–13) but 15 Shipping Quotes (0–14) are seeded; the last quote is unreachable. | `SailorsCode`, `V4__quotes.sql` | Minor; one quote never shown. |
| R-3 | Debt | A rejected cargo load (too heavy / already loaded) returns `200` with the unchanged ship; other rule violations surface as `500`. | `CargoLoadManagementService`, controllers | Client cannot tell success from rejection; see 8.4. |
| R-4 | Risk | Kafka and Debezium are not part of the Kubernetes deployment; Releases on the cluster are written to the outbox but never published. | `k8s/`, ch. 7 | The outbox table grows; the terminal sees nothing on k8s. Once Harbors exchange ships (ADR-0004), voyages cannot work on k8s at all. |
| R-5 | Risk | Credentials are in plain text in `application.yml`, the Compose files and the connector JSON; `k8s/10-secrets.yaml` (base64) is committed. | config files | Acceptable only for a playground; must not be reused elsewhere. |
| R-6 | Debt | A second Debezium connector streams the `catains` table to Kafka with no consumer. | `kafka-connect/connectors/catain-connector_*.json` | Unused infrastructure, unclear intent. |
| R-7 | Debt | No automated tests exist. | whole repo | Every change is verified by hand; see 8.8. |
| R-8 | Risk | `ship-terminal` duplicates the event classes instead of sharing a schema. | `ship-terminal/Main.kt` | Event changes can silently break the consumer (unknown fields are ignored, removed ones are not). |
| R-9 | Debt | Images are deployed as `:latest`; the frontend image is not built by CI. | ch. 7.3 | Deployments are not reproducible. |
| R-10 | Risk | A return Arrival processed before the earlier `ship-arrived` for the same ship is skipped as a duplicate ("Ship Id already in the fleet"), and the ship is lost at that Harbor. Between two Harbors the order is guaranteed; it can happen on a round trip through three or more Harbors when one Harbor's connector lags, or on a repartitioned `hexagonship-shipping`. | `ArrivalManagementService`, the `hexagonship-shipping` key (Shipping id) | A ship can vanish from every fleet. A fix (key both events by Ship Id, or end the voyage of an in-fleet ship at sea implicitly on Arrival) needs its own story. |
