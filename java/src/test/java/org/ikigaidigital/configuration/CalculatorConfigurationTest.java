package org.ikigaidigital.configuration;

import org.ikigaidigital.calculator.DepositBalanceCalculator;
import org.ikigaidigital.calculator.plan.PlanInterestCalculator;
import org.ikigaidigital.domain.TimeDeposit;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CalculatorConfigurationTest {
    @Test
    void wiresEveryBuiltInPlanIntoTheBalanceCalculator() {
        new ApplicationContextRunner()
                .withUserConfiguration(CalculatorConfiguration.class)
                .run(context -> {
                    assertThat(context.getBeansOfType(PlanInterestCalculator.class).values())
                            .extracting(PlanInterestCalculator::getPlanName)
                            .containsExactlyInAnyOrder("basic", "student", "premium");

                    var deposits = List.of(
                            new TimeDeposit(1, "basic", 1200.00, 46),
                            new TimeDeposit(2, "student", 1200.00, 46),
                            new TimeDeposit(3, "premium", 1200.00, 46)
                    );
                    context.getBean(DepositBalanceCalculator.class).updateBalance(deposits);

                    assertThat(deposits).extracting(TimeDeposit::getBalance)
                            .containsExactly(1201.00, 1203.00, 1205.00);
                });
    }
}
