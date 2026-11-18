package org.ikigaidigital.calculator;

import org.ikigaidigital.domain.TimeDeposit;

import java.util.List;

public interface DepositBalanceCalculator {
    /**
     * Validates every plan type before updating balances in place.
     *
     * @throws InvalidPlanTypeException if a deposit refers to an unregistered plan type
     */
    void updateBalance(List<TimeDeposit> deposits);
}
