package org.ikigaidigital.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TimeDepositRepository extends JpaRepository<TimeDepositEntity, Integer> {
    @Query("select d from TimeDepositEntity d left join fetch d.withdrawals order by d.id")
    List<TimeDepositEntity> findAllWithWithdrawals();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<TimeDepositEntity> findFirstByOrderByIdAsc();

    List<TimeDepositEntity> findByIdGreaterThanEqualOrderByIdAsc(int firstId, Pageable pageable);
}
