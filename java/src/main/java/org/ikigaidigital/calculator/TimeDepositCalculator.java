package org.ikigaidigital.calculator;

import org.ikigaidigital.calculator.plan.PlanInterestCalculator;
import org.ikigaidigital.domain.TimeDeposit;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TimeDepositCalculator implements DepositBalanceCalculator {
    private final Map<String, PlanInterestCalculator> calculatorsByName;

    public TimeDepositCalculator(Collection<? extends PlanInterestCalculator> planCalculators) {
        calculatorsByName = planCalculators.stream()
                .collect(Collectors.toUnmodifiableMap(PlanInterestCalculator::getPlanName, Function.identity()));
    }

    @Override
    public void updateBalance(List<TimeDeposit> deposits) {
        // Validate the whole list before crediting interest so a retry cannot credit earlier deposits twice.
        for (TimeDeposit deposit : deposits) {
            String planType = deposit.getPlanType();
            if (planType == null || !calculatorsByName.containsKey(planType)) {
                throw new InvalidPlanTypeException(planType);
            }
        }

        for (TimeDeposit deposit : deposits) {
            double balance = deposit.getBalance();
            var planCalculator = calculatorsByName.get(deposit.getPlanType());
            double interest = planCalculator.calculateInterest(balance, deposit.getDays());

            // Round the exact binary value to preserve the legacy half-cent behavior.
            double roundedInterest = new BigDecimal(interest).setScale(2, RoundingMode.HALF_UP).doubleValue();
            // Add as doubles because decimal addition can change the legacy balance.
            deposit.setBalance(balance + roundedInterest);
        }
    }
}
