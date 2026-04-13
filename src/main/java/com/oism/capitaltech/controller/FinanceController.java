package com.oism.capitaltech.controller;

import com.oism.capitaltech.dto.FinanceSummaryResponse;
import com.oism.capitaltech.service.FinanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/finance")
public class FinanceController {

    private final FinanceService financeService;

    public FinanceController(FinanceService financeService) {
        this.financeService = financeService;
    }

    @GetMapping("/summary")
    public ResponseEntity<FinanceSummaryResponse> summary() {
        return ResponseEntity.ok(financeService.getSummary());
    }
}
