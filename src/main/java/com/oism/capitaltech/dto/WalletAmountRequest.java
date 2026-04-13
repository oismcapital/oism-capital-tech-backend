package com.oism.capitaltech.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record WalletAmountRequest(
        @NotNull @Positive BigDecimal valor,
        @NotBlank String descricao
) {
}

