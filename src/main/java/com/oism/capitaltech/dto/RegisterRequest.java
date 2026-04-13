package com.oism.capitaltech.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record RegisterRequest(
        @NotBlank String nome,
        @Email @NotBlank String email,
        @NotBlank String senha,
        @NotNull @PositiveOrZero BigDecimal saldo
) {
}
