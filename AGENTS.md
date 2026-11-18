# Time Deposit Kata

Start with the [assignment README](README.md) for the requirements, preferred stack, and submission instructions. Use [SOLUTION.md](SOLUTION.md) for implementation and run instructions.

## Project basics

- Implement the assignment in `java/` using Java 25 and Spring Boot. Run `./mvnw test` from `java/` with Docker available for PostgreSQL Testcontainers.
- Preserve the public `TimeDeposit` API, the signature of `TimeDepositCalculator.updateBalance(List<TimeDeposit>)`, and the existing interest behavior for supported plans. Add characterization tests before refactoring the interest calculation.
- Deliver exactly two API endpoints and document them in an OpenAPI contract. Map PostgreSQL tables with Spring Data JPA and test persistence against PostgreSQL Testcontainers.
- Fetch deposits and withdrawals together for GET. Keep the in-memory accrual batches bounded.
- Develop on the personal fork. Keep commits focused on one change each.

## Spec-driven development and ADRs

1. Start with the README, the [business assumptions](SOLUTION.md#business-assumptions), and any accepted architecture decision records (ADRs). For changes where the README leaves important behavior open, write a short spec under `docs/specs/` covering observable behavior, assumptions, and acceptance checks. Use the OpenAPI contract as the spec for HTTP behavior.
2. Record consequential architecture choices in `docs/adr/NNNN-short-title.md`. Include status, context, decision, alternatives, consequences, and how to verify the claims. Start as `Proposed`, use `Accepted` once adopted, and link to the replacement if a decision is `Superseded`.
3. Implement against the spec, verify with focused tests, and record the tools, repository rules, and work that were AI-assisted for the submission.

The [architecture decision index](SOLUTION.md#architecture-decisions) lists the accepted ADRs.
