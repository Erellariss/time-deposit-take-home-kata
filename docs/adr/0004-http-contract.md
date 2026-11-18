# ADR 0004 — Define the HTTP contract at the controller boundary

**Status** Accepted

## Context

Clients need a Swagger contract for two operations on a prefilled portfolio. The shared domain model represents calculator inputs, while API clients also need withdrawal details.

## Decision

`TimeDepositApi` declares Spring MVC routes, HTTP statuses, and OpenAPI annotations. The controller implements that interface and delegates both synchronous operations to `TimeDepositService`. The service maps stored deposits and withdrawals to dedicated response records.

- `GET /time-deposits` returns an array with `id`, `planType`, `balance`, `days`, and `withdrawals` for each deposit. An empty portfolio returns `[]`.
- `POST /time-deposits/accruals` takes no request body. It returns `204` after the transaction commits. Each successful request credits another month, including a retry of a request that already succeeded.

## Alternatives and consequences

Serializing JPA entities directly would make the external schema depend on persistence mappings. Dedicated records require explicit mapping and allow the database model to evolve independently. Synchronous accrual gives callers a committed outcome in the same request and keeps the connection open for the duration of the portfolio update.

## Verification

HTTP tests check response fields, empty results, the committed accrual, and rollback on failure. They also inspect the generated OpenAPI document for the two operations, the nested response schema, and the bodyless POST. [Endpoint tests](../../java/src/test/java/org/ikigaidigital/api/TimeDepositApiTest.java)
