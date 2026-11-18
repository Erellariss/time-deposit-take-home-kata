package org.ikigaidigital.calculator.plan;

public final class StudentInterestCalculator implements PlanInterestCalculator {
    @Override
    public String getPlanName() {
        return "student";
    }

    @Override
    public double calculateInterest(double balance, int days) {
        return days > 30 && days <= 365 ? balance * 0.03 / 12 : 0.0;
    }
}
