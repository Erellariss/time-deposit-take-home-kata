package org.ikigaidigital.calculator.plan;

public final class PremiumInterestCalculator implements PlanInterestCalculator {
    @Override
    public String getPlanName() {
        return "premium";
    }

    @Override
    public double calculateInterest(double balance, int days) {
        return days > 45 ? balance * 0.05 / 12 : 0.0;
    }
}
