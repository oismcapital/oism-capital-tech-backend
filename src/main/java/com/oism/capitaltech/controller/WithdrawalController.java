package com.oism.capitaltech.controller;

import com.oism.capitaltech.dto.WithdrawalRequestDto;
import com.oism.capitaltech.dto.WithdrawalResponse;
import com.oism.capitaltech.service.WithdrawalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/withdrawals")
public class WithdrawalController {

    private final WithdrawalService withdrawalService;

    public WithdrawalController(WithdrawalService withdrawalService) {
        this.withdrawalService = withdrawalService;
    }

    /** Request a PIX withdrawal. POST /api/v1/withdrawals */
    @PostMapping
    public ResponseEntity<WithdrawalResponse> request(@Valid @RequestBody WithdrawalRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(withdrawalService.requestWithdrawal(dto));
    }

    /** List all withdrawals for the authenticated user. GET /api/v1/withdrawals */
    @GetMapping
    public ResponseEntity<List<WithdrawalResponse>> list() {
        return ResponseEntity.ok(withdrawalService.listMyWithdrawals());
    }

    /** Mark a withdrawal as completed (admin/webhook). POST /api/v1/withdrawals/{id}/complete */
    @PostMapping("/{id}/complete")
    public ResponseEntity<WithdrawalResponse> complete(@PathVariable Long id) {
        return ResponseEntity.ok(withdrawalService.complete(id));
    }

    /** Mark a withdrawal as failed and refund. POST /api/v1/withdrawals/{id}/fail */
    @PostMapping("/{id}/fail")
    public ResponseEntity<WithdrawalResponse> fail(
            @PathVariable Long id,
            @RequestParam(defaultValue = "Processing error") String reason) {
        return ResponseEntity.ok(withdrawalService.fail(id, reason));
    }
}
