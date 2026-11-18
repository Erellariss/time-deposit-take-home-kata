package org.ikigaidigital.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(requiredProperties = {"id", "amount", "date"})
public record WithdrawalResponse(int id, BigDecimal amount, LocalDate date) {
}
