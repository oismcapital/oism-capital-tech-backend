package com.oism.capitaltech.dto;

import com.oism.capitaltech.entity.WalletTransaction;
import com.oism.capitaltech.entity.WalletTransactionType;

import java.math.BigDecimal;
import java.time.Instant;

public record WalletTransactionResponse(
        Long id,
        WalletTransactionType type,
        BigDecimal amount,
        BigDecimal balanceBefore,
        BigDecimal balanceAfter,
        Instant createdAt,
        String metadataJson
) {
    public static WalletTransactionResponse fromEntity(WalletTransaction t) {
        return new WalletTransactionResponse(
                t.getId(),
                t.getType(),
                t.getAmount(),
                t.getBalanceBefore(),
                t.getBalanceAfter(),
                t.getCreatedAt(),
                t.getMetadataJson()
        );
    }
}

