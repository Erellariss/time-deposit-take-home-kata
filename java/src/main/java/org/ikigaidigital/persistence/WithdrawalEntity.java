package org.ikigaidigital.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "withdrawals", indexes = @Index(name = "withdrawals_deposit_id_idx", columnList = "time_deposit_id, id"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WithdrawalEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "time_deposit_id", nullable = false)
    private TimeDepositEntity timeDeposit;

    @Column(nullable = false, columnDefinition = "numeric")
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate date;

    public WithdrawalEntity(TimeDepositEntity timeDeposit, BigDecimal amount, LocalDate date) {
        this.timeDeposit = timeDeposit;
        this.amount = amount;
        this.date = date;
    }
}
