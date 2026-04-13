package com.oism.capitaltech.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PixWebhookRequest(
        @NotBlank String transactionId,
        @NotNull Long userId,
        @NotNull BigDecimal valor,
        @NotBlank String status
) {
}
