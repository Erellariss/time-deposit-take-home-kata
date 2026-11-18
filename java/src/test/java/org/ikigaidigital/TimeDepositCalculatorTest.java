package org.ikigaidigital;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class TimeDepositCalculatorTest {
    private static final String BASIC = "basic";
    private static final String STUDENT = "student";
    private static final String PREMIUM = "premium";

    @ParameterizedTest(name = "{0}")
    @MethodSource("balanceCases")
    void updatesBalance(TimeDeposit deposit, double expectedBalance) {
        new TimeDepositCalculator().updateBalance(List.of(deposit));

        assertThat(deposit.getBalance()).isEqualTo(expectedBalance);
    }

    @Test
    void updatesEligibleDepositsInAMixedList() {
        var deposits = List.of(
                new TimeDeposit(1, BASIC, 1200.00, 31),
                new TimeDeposit(2, STUDENT, 1200.00, 366),
                new TimeDeposit(3, PREMIUM, 1200.00, 46)
        );

        new TimeDepositCalculator().updateBalance(deposits);

        assertThat(deposits)
                .extracting(TimeDeposit::getId, TimeDeposit::getPlanType, TimeDeposit::getDays, TimeDeposit::getBalance)
                .containsExactly(
                        tuple(1, BASIC, 31, 1201.00),
                        tuple(2, STUDENT, 366, 1200.00),
                        tuple(3, PREMIUM, 46, 1205.00)
                );
    }

    @Test
    void usesUpdatedBalanceOnTheNextCall() {
        var deposit = new TimeDeposit(1, STUDENT, 40000.00, 31);
        var deposits = List.of(deposit);
        var calculator = new TimeDepositCalculator();

        calculator.updateBalance(deposits);
        assertThat(deposit.getBalance()).isEqualTo(40100.00);

        calculator.updateBalance(deposits);
        assertThat(deposit.getBalance()).isEqualTo(40200.25);
    }

    @Test
    void acceptsAnEmptyList() {
        new TimeDepositCalculator().updateBalance(List.of());
    }

    private static Stream<Arguments> balanceCases() {
        return Stream.of(
                balanceCase(new TimeDeposit(1, BASIC, 1200.00, 30), 1200.00),
                balanceCase(new TimeDeposit(1, BASIC, 1200.00, 31), 1201.00),
                balanceCase(new TimeDeposit(1, BASIC, 1200.00, 366), 1201.00),
                balanceCase(new TimeDeposit(1, STUDENT, 1200.00, 30), 1200.00),
                balanceCase(new TimeDeposit(1, STUDENT, 1200.00, 31), 1203.00),
                balanceCase(new TimeDeposit(1, STUDENT, 1200.00, 365), 1203.00),
                balanceCase(new TimeDeposit(1, STUDENT, 1200.00, 366), 1200.00),
                balanceCase(new TimeDeposit(1, PREMIUM, 1200.00, 30), 1200.00),
                balanceCase(new TimeDeposit(1, PREMIUM, 1200.00, 45), 1200.00),
                balanceCase(new TimeDeposit(1, PREMIUM, 1200.00, 46), 1205.00),
                balanceCase(new TimeDeposit(1, PREMIUM, 1200.00, 366), 1205.00),
                // HALF_UP rounds interest to the nearest cent, with exact ties away from zero.
                balanceCase(new TimeDeposit(1, BASIC, 5.99, 31), 5.99),
                // In decimal math, $6 earns $0.005 and $18 earns $0.015. The legacy double
                // results land just above $0.005 and just below $0.015, so both credit one cent.
                balanceCase(new TimeDeposit(1, BASIC, 6.00, 31), 6.01),
                balanceCase(new TimeDeposit(1, BASIC, 18.00, 31), 18.01),
                // Round the interest before adding it to the original balance, preserving its extra decimal place.
                balanceCase(new TimeDeposit(1, BASIC, 1200.125, 31), 1201.125),
                balanceCase(new TimeDeposit(1, BASIC, 1234567.00, 45), 1235595.81)
        );
    }

    private static Arguments balanceCase(TimeDeposit deposit, double expectedBalance) {
        var description = "%s on day %d with balance %s"
                .formatted(deposit.getPlanType(), deposit.getDays(), deposit.getBalance());
        return arguments(named(description, deposit), expectedBalance);
    }
}
