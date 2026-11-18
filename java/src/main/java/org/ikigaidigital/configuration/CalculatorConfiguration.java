package org.ikigaidigital.configuration;

import org.ikigaidigital.calculator.DepositBalanceCalculator;
import org.ikigaidigital.calculator.TimeDepositCalculator;
import org.ikigaidigital.calculator.plan.BasicInterestCalculator;
import org.ikigaidigital.calculator.plan.PlanInterestCalculator;
import org.ikigaidigital.calculator.plan.PremiumInterestCalculator;
import org.ikigaidigital.calculator.plan.StudentInterestCalculator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration(proxyBeanMethods = false)
class CalculatorConfiguration {
    @Bean
    BasicInterestCalculator basicInterestCalculator() {
        return new BasicInterestCalculator();
    }

    @Bean
    StudentInterestCalculator studentInterestCalculator() {
        return new StudentInterestCalculator();
    }

    @Bean
    PremiumInterestCalculator premiumInterestCalculator() {
        return new PremiumInterestCalculator();
    }

    @Bean
    DepositBalanceCalculator depositBalanceCalculator(List<PlanInterestCalculator> planCalculators) {
        return new TimeDepositCalculator(planCalculators);
    }
}
