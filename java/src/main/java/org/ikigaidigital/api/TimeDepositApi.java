package org.ikigaidigital.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.ikigaidigital.dto.TimeDepositResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RequestMapping("/time-deposits")
public interface TimeDepositApi {
    @GetMapping(produces = APPLICATION_JSON_VALUE)
    @Operation(operationId = "listTimeDeposits", summary = "List all time deposits and their withdrawals")
    @ApiResponse(responseCode = "200", description = "Deposits in ID order with withdrawals in ID order",
            content = @Content(mediaType = APPLICATION_JSON_VALUE,
                    array = @ArraySchema(schema = @Schema(implementation = TimeDepositResponse.class))))
    List<TimeDepositResponse> listTimeDeposits();

    @PostMapping("/accruals")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "accrueInterest", summary = "Apply monthly interest to all eligible deposits",
            description = "Each successful call applies one monthly accrual. Repeated calls compound interest.")
    @ApiResponse(responseCode = "204", description = "Accrual committed", content = @Content)
    void accrueInterest();
}
