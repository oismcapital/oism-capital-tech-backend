package com.oism.capitaltech.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record WithdrawalRequestDto(
        @NotNull(message = "amount is required")
        @DecimalMin(value = "0.01", message = "Minimum withdrawal amount is R$ 0.01")
        BigDecimal amount,

        @NotBlank(message = "pixKey is required")
        String pixKey
) {}
