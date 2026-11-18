package org.ikigaidigital.calculator.plan;

public final class BasicInterestCalculator implements PlanInterestCalculator {
    @Override
    public String getPlanName() {
        return "basic";
    }

    @Override
    public double calculateInterest(double balance, int days) {
        return days > 30 ? balance * 0.01 / 12 : 0.0;
    }
}
