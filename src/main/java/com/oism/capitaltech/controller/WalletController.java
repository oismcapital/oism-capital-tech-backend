package com.oism.capitaltech.controller;

import com.oism.capitaltech.dto.UserResponse;
import com.oism.capitaltech.dto.WalletAmountRequest;
import com.oism.capitaltech.dto.WalletPreferencesRequest;
import com.oism.capitaltech.dto.WalletTransactionResponse;
import com.oism.capitaltech.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me() {
        return ResponseEntity.ok(walletService.me());
    }

    @PostMapping("/deposit")
    public ResponseEntity<UserResponse> deposit(@Valid @RequestBody WalletAmountRequest request) {
        return ResponseEntity.ok(walletService.deposit(request));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<UserResponse> withdraw(@Valid @RequestBody WalletAmountRequest request) {
        return ResponseEntity.ok(walletService.withdraw(request));
    }

    @GetMapping("/statement")
    public ResponseEntity<List<WalletTransactionResponse>> statement(@RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(walletService.statement(limit));
    }

    @PatchMapping("/preferences")
    public ResponseEntity<Void> updatePreferences(@Valid @RequestBody WalletPreferencesRequest request) {
        walletService.updatePreferences(request);
        return ResponseEntity.noContent().build();
    }
}
