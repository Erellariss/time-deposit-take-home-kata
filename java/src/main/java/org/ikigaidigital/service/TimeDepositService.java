package org.ikigaidigital.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.ikigaidigital.calculator.DepositBalanceCalculator;
import org.ikigaidigital.domain.TimeDeposit;
import org.ikigaidigital.dto.TimeDepositResponse;
import org.ikigaidigital.dto.WithdrawalResponse;
import org.ikigaidigital.persistence.TimeDepositEntity;
import org.ikigaidigital.persistence.TimeDepositRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TimeDepositService {
    private static final PageRequest BATCH = PageRequest.of(0, 1000);

    private final TimeDepositRepository deposits;
    private final DepositBalanceCalculator calculator;
    private final EntityManager entityManager;

    public List<TimeDepositResponse> listTimeDeposits() {
        return deposits.findAllWithWithdrawals().stream().map(TimeDepositService::toResponse).toList();
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void accrueInterest() {
        // Every run locks the same first deposit until commit so the next run reads the credited balances.
        var first = deposits.findFirstByOrderByIdAsc();
        if (first.isEmpty()) {
            return;
        }

        int firstId = first.get().getId();
        while (true) {
            var batch = deposits.findByIdGreaterThanEqualOrderByIdAsc(firstId, BATCH);
            updateBalances(batch);
            entityManager.flush();
            entityManager.clear();

            if (batch.size() < BATCH.getPageSize() || batch.getLast().getId() == Integer.MAX_VALUE) {
                return;
            }
            firstId = batch.getLast().getId() + 1;
        }
    }

    private void updateBalances(List<TimeDepositEntity> batch) {
        var domainDeposits = batch.stream()
                .map(row -> new TimeDeposit(row.getId(), row.getPlanType(), row.getBalance().doubleValue(), row.getDays()))
                .toList();
        calculator.updateBalance(domainDeposits);

        for (int i = 0; i < batch.size(); i++) {
            var row = batch.get(i);
            double updated = domainDeposits.get(i).getBalance();
            if (updated != row.getBalance().doubleValue()) {
                row.setBalance(BigDecimal.valueOf(updated));
            }
        }
    }

    private static TimeDepositResponse toResponse(TimeDepositEntity deposit) {
        var withdrawals = deposit.getWithdrawals().stream()
                .map(withdrawal -> new WithdrawalResponse(withdrawal.getId(), withdrawal.getAmount(), withdrawal.getDate()))
                .toList();
        return new TimeDepositResponse(deposit.getId(), deposit.getPlanType(), deposit.getBalance(),
                deposit.getDays(), withdrawals);
    }
}
