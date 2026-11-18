package org.ikigaidigital.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

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
    private BigDecimal balance;

    public TimeDepositEntity(String planType, int days, BigDecimal balance) {
        this.planType = planType;
        this.days = days;
        this.balance = balance;
    }
}
