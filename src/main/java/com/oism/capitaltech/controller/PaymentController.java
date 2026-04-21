package com.oism.capitaltech.controller;

import com.oism.capitaltech.dto.DepositPixRequest;
import com.oism.capitaltech.dto.DepositPixResponse;
import com.oism.capitaltech.dto.DepositStatusResponse;
import com.oism.capitaltech.dto.GeneratePixRequest;
import com.oism.capitaltech.dto.GeneratePixResponse;
import com.oism.capitaltech.dto.PlanResponse;
import com.oism.capitaltech.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Lista todos os planos disponíveis.
     * GET /api/v1/payments/plans
     */
    @GetMapping("/plans")
    public ResponseEntity<List<PlanResponse>> listPlans() {
        return ResponseEntity.ok(PlanResponse.allPlans());
    }

    /**
     * Consulta o status de um depósito Pix.
     * GET /api/v1/payments/{transactionId}/status
     */
    @GetMapping("/{transactionId}/status")
    public ResponseEntity<DepositStatusResponse> status(@PathVariable String transactionId) {
        return ResponseEntity.ok(paymentService.getStatus(transactionId));
    }

    /**
     * Gera PIX de depósito livre (qualquer valor) → vai para o saldo da Wallet.
     * POST /api/v1/payments/deposit-pix
     * Body: { "valor": 150.00 }
     */
    @PostMapping("/deposit-pix")
    public ResponseEntity<DepositPixResponse> depositPix(@Valid @RequestBody DepositPixRequest request) {
        return ResponseEntity.ok(paymentService.generateDepositPix(request));
    }

    /**
     * Gera uma cobrança Pix dinâmica para o plano selecionado.
     * POST /api/v1/payments/generate-pix
     * Body: { "planId": "START" }
     */
    @PostMapping("/generate-pix")
    public ResponseEntity<GeneratePixResponse> generatePix(@Valid @RequestBody GeneratePixRequest request) {
        return ResponseEntity.ok(paymentService.generatePix(request));
    }
}
