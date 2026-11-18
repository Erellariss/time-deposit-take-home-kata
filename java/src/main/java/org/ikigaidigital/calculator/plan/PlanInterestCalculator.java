package org.ikigaidigital.calculator.plan;

public interface PlanInterestCalculator {
    String getPlanName();

    /** Returns the unrounded interest for one month. */
    double calculateInterest(double balance, int days);
}
