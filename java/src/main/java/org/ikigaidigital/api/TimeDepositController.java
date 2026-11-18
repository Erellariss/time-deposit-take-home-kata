package org.ikigaidigital.api;

import lombok.RequiredArgsConstructor;
import org.ikigaidigital.dto.TimeDepositResponse;
import org.ikigaidigital.service.TimeDepositService;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TimeDepositController implements TimeDepositApi {
    private final TimeDepositService service;

    @Override
    public List<TimeDepositResponse> listTimeDeposits() {
        return service.listTimeDeposits();
    }

    @Override
    public void accrueInterest() {
        service.accrueInterest();
    }
}
