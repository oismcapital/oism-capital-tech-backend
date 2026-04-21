package com.oism.capitaltech.dto;

import com.oism.capitaltech.entity.WithdrawalRequest;

import java.math.BigDecimal;
import java.time.Instant;

public record WithdrawalResponse(
        Long id,
        BigDecimal amount,
        String pixKey,
        String status,
        Instant requestedAt,
        Instant processedAt,
        String failureReason
) {
    public static WithdrawalResponse fromEntity(WithdrawalRequest w) {
        return new WithdrawalResponse(
                w.getId(),
                w.getAmount(),
                w.getPixKey(),
                w.getStatus().name(),
                w.getRequestedAt(),
                w.getProcessedAt(),
                w.getFailureReason()
        );
    }
}
