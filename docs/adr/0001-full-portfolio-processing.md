# ADR 0001 — Full-portfolio processing with Spring Data

**Status** Accepted

## Context

The assignment requires one endpoint to list every deposit with its withdrawals and another to update every deposit's balance. Both must handle growing datasets. The existing Java calculator fixes details such as the day cutoffs and how interest is rounded.

## Decision

- `TimeDepositService` uses the existing Spring Data repository for both operations. It fetches deposits and withdrawals in one ordered join and maps them to HTTP response records. The withdrawal index supports the join and ordering.
- For accrual, read deposits in batches of 1,000 using the primary key and calculate interest in Java. Flush and clear the persistence context after each batch. Lock the first deposit for the duration of the transaction so concurrent runs read the latest committed balances.

## Alternatives and consequences

- GET materializes the full result for simplicity. Its heap use grows with the number of deposits and withdrawals. The fetch join keeps SQL reads at one query for the listing.
- The accrual transaction grows with the number of changed deposits. Hibernate batches the updates and the application keeps only one batch of managed deposits in memory.
- Concurrent accruals serialize on the deposit with the lowest ID. This relies on a prefilled portfolio whose membership stays fixed during accrual. An empty portfolio requires no update. Revisit the locking decision if deposits can be inserted or deleted during a run.

## Verification

Use PostgreSQL Testcontainers to check the GET shape, accrual across multiple batches, rollback after a later batch fails, and concurrent accrual. Measure elapsed time and peak JVM heap on a growing dataset before claiming a capacity target.
