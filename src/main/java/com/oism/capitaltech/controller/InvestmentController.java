package com.oism.capitaltech.controller;

import com.oism.capitaltech.dto.InvestmentResponse;
import com.oism.capitaltech.dto.PurchasePlanRequest;
import com.oism.capitaltech.service.InvestmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/investments")
public class InvestmentController {

    private final InvestmentService investmentService;

    public InvestmentController(InvestmentService investmentService) {
        this.investmentService = investmentService;
    }

    /** Contrata um plano debitando da Wallet. POST /api/v1/investments */
    @PostMapping
    public ResponseEntity<InvestmentResponse> purchase(@Valid @RequestBody PurchasePlanRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(investmentService.purchase(request));
    }

    /** Lista todos os investimentos do usuário. GET /api/v1/investments */
    @GetMapping
    public ResponseEntity<List<InvestmentResponse>> listAll() {
        return ResponseEntity.ok(investmentService.listAll());
    }

    /** Lista apenas investimentos ativos. GET /api/v1/investments/active */
    @GetMapping("/active")
    public ResponseEntity<List<InvestmentResponse>> listActive() {
        return ResponseEntity.ok(investmentService.listActive());
    }

    /** Resgata o lucro acumulado (D+15). POST /api/v1/investments/{id}/withdraw-interest */
    @PostMapping("/{id}/withdraw-interest")
    public ResponseEntity<InvestmentResponse> withdrawInterest(@PathVariable Long id) {
        return ResponseEntity.ok(investmentService.withdrawInterest(id));
    }
}
