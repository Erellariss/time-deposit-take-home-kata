package org.ikigaidigital.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(requiredProperties = {"id", "planType", "balance", "days", "withdrawals"})
public record TimeDepositResponse(
        int id,
        String planType,
        BigDecimal balance,
        int days,
        @Schema(description = "Empty when the deposit has no withdrawals") List<WithdrawalResponse> withdrawals
) {
}
