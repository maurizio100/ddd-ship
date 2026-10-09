# ship-backend — API design

## Resources

- REST/JSON under `/web`. Collections are plural nouns (`/web/ships`, `/web/cargos`, `/web/catains`).
- Things that belong to a ship are nested under it: `/web/ships/{shipId}/cargos`,
  `/web/ships/{shipId}/shippings`.
- Path ids are the UUID business ids, never the database surrogate key.
- Responses return the resource directly: no envelope, no pagination, no versioning.
- A command on a ship returns the updated ship (`ShipDetailResponse`) so the client doesn't need a
  second request.
- `ship-backend/openapi.yml` is the contract and is updated in the same change as the endpoint.
- `GET /web/fleet-events` is a `text/event-stream` (Server-Sent Events), not a resource. Its event names
  (`ship-arrived`, `ship-left`) and their data schemas are documented in `openapi.yml` like any endpoint.
  It answers with `Cache-Control: no-cache` and `X-Accel-Buffering: no`.

## Errors

- The domain signals a rule violation by throwing a typed exception from `domain/exception`. A
  rejected command is never answered with `200` and an unchanged resource.
- One `@RestControllerAdvice` in `driving-adapter` maps exceptions to **RFC 9457 Problem Details**
  (Spring `ProblemDetail`, `application/problem+json`):

  | Situation | Status |
  |---|---|
  | Resource not found (port returns `null`) | `404` |
  | Malformed or invalid request input (missing field, blank Ship Name) | `400` |
  | Domain rule violation (Max Weight exceeded, Cargo out of Stock, second Active Shipping) | `409` |
  | Anything else | `500`, with no internal detail in the body |

- `title` is a short fixed phrase per exception type. `detail` explains this case in domain
  language ("Loading Rum would exceed the Max Weight of 15.0").
- Controllers don't build error responses themselves. They return the port's result or throw.

The handler is `ProblemDetailsExceptionHandler`. It extends `ResponseEntityExceptionHandler`, so a
`ResponseStatusException` thrown for a `404` or `400` is rendered as Problem Details too. Some existing
endpoints still let rule violations (`IllegalArgumentException`, `IllegalStateException`) surface as
`500`. Bring an endpoint up to these rules when a story changes it.
