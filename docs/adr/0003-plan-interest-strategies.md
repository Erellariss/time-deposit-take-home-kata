# ADR 0003 — Keep plan strategies independent of Spring

**Status** Accepted

## Context

Interest rates and eligibility periods can change independently for each plan. The calculator needs to support new plans while keeping its public balance-update operation stable and its rules testable in plain Java.

## Decision

Each plan implements `getPlanName()` and `calculateInterest(double balance, int days)`. The strategy owns eligibility and returns unrounded monthly interest. Spring configuration registers the strategies as beans and passes their collection to the calculator constructor. The calculator indexes the strategies once by their exact plan names. A case or whitespace difference is significant.

Before updating a supplied list, the calculator checks that every plan is registered. An unknown plan raises `InvalidPlanTypeException` before any deposit in that list is changed.

## Alternatives and consequences

A switch in the calculator would require editing the dispatcher for every new plan. With this design, adding a plan requires a strategy and bean registration. Basic and Student each express the 30-day cutoff, while Premium's 45-day cutoff already covers it. Sharing one waiting-period check would couple rules that may later diverge.

## Verification

Plain Java tests inject a custom strategy, exercise the built-in eligibility boundaries, and check that an invalid plan leaves earlier balances intact. A Spring wiring test verifies registration of the built-in strategies. [Calculator tests](../../java/src/test/java/org/ikigaidigital/calculator/TimeDepositCalculatorTest.java) · [Wiring test](../../java/src/test/java/org/ikigaidigital/configuration/CalculatorConfigurationTest.java)
