package com.oism.capitaltech.dto;

import com.oism.capitaltech.entity.PixDeposit;

public record DepositStatusResponse(
        String transactionId,
        String status,
        String planName,
        String expiresAt,
        String completedAt
) {
    public static DepositStatusResponse fromEntity(PixDeposit d) {
        return new DepositStatusResponse(
                d.getTransactionId(),
                d.getStatus().name(),
                d.getPlan().getDisplayName(),
                d.getExpiresAt() != null ? d.getExpiresAt().toString() : null,
                d.getCompletedAt() != null ? d.getCompletedAt().toString() : null
        );
    }
}
