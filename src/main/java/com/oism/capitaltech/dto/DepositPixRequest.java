package com.oism.capitaltech.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record DepositPixRequest(
        @NotNull(message = "valor é obrigatório")
        @DecimalMin(value = "1.00", message = "Valor mínimo de depósito é R$ 1,00")
        BigDecimal valor
) {}
