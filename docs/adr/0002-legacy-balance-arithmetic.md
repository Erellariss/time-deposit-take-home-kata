# ADR 0002 — Preserve legacy balance arithmetic

**Status** Accepted

## Context

The shared Java model exposes a `Double` balance. Existing callers rely on the observable results of `updateBalance(List<TimeDeposit>)`, including how it handles half-cent interest. PostgreSQL stores balances as `numeric`. Introducing the database raised the question of which calculation determines a posted balance.

## Decision

Use the existing `updateBalance` operation for every accrual, including each database batch. The plan returns unrounded monthly interest as a `double`. The calculator rounds that exact binary value with `new BigDecimal(interest).setScale(2, HALF_UP)`, converts the rounded interest to a `double`, and adds it to the balance.

The service writes a changed result using `BigDecimal.valueOf(updated)`. If the calculator leaves the balance unchanged, the service keeps the original database value, including precision beyond what a `double` can represent.

## Alternatives and consequences

Using decimal arithmetic throughout would change posted balances. On day 31, a Basic deposit of `18.00` earns `0.01` because its computed interest falls just below the decimal half-cent boundary of `0.015`. Adding that rounded credit to an original SQL balance would also introduce a second addition rule. Under the legacy rule, a Basic balance of `6.02` on day 31 becomes `6.029999999999999`. A change to these results requires an explicit decision about compatibility.

## Verification

The parameterized calculator tests cover half-cent rounding and day boundaries. The endpoint tests read PostgreSQL balances after accrual, including both the `6.02` case and an ineligible deposit with extra decimal places. [Calculator tests](../../java/src/test/java/org/ikigaidigital/calculator/TimeDepositCalculatorTest.java) · [Endpoint tests](../../java/src/test/java/org/ikigaidigital/api/TimeDepositApiTest.java)
