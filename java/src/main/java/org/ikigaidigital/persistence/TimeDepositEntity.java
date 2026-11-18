package org.ikigaidigital.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "time_deposits")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TimeDepositEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "plan_type", nullable = false)
    private String planType;

    @Column(nullable = false)
    private int days;

    @Column(nullable = false, columnDefinition = "numeric")
    @Setter
    private BigDecimal balance;

    @OneToMany(mappedBy = "timeDeposit")
    @OrderBy("id ASC")
    private List<WithdrawalEntity> withdrawals = new ArrayList<>();

    public TimeDepositEntity(String planType, int days, BigDecimal balance) {
        this.planType = planType;
        this.days = days;
        this.balance = balance;
    }
}
