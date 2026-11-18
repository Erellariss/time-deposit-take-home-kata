package org.ikigaidigital.persistence;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class DepositPersistenceTest {
    @Autowired
    private TimeDepositRepository deposits;

    @Autowired
    private WithdrawalRepository withdrawals;

    @Autowired
    private EntityManager entityManager;

    @Test
    void savesAndReloadsADepositWithAWithdrawalInTheGeneratedSchema() {
        var balance = new BigDecimal("1200.125");
        var amount = new BigDecimal("12.345");
        var date = LocalDate.of(2026, 1, 15);
        var deposit = deposits.save(new TimeDepositEntity("basic", 46, balance));
        var withdrawal = withdrawals.saveAndFlush(new WithdrawalEntity(deposit, amount, date));
        entityManager.clear();

        var storedWithdrawal = withdrawals.findById(withdrawal.getId()).orElseThrow();
        assertThat(storedWithdrawal.getAmount()).isEqualByComparingTo(amount);
        assertThat(storedWithdrawal.getDate()).isEqualTo(date);

        var storedDeposit = storedWithdrawal.getTimeDeposit();
        assertThat(storedDeposit.getId()).isEqualTo(deposit.getId());
        assertThat(storedDeposit.getPlanType()).isEqualTo("basic");
        assertThat(storedDeposit.getDays()).isEqualTo(46);
        assertThat(storedDeposit.getBalance()).isEqualByComparingTo(balance);
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class PostgresConfiguration {
        @Bean
        @ServiceConnection
        PostgreSQLContainer postgres() {
            return new PostgreSQLContainer("postgres:17-alpine");
        }
    }
}
