package com.oism.capitaltech.dto;

import jakarta.validation.constraints.NotBlank;

public record GeneratePixRequest(
        @NotBlank(message = "planId é obrigatório")
        String planId
) {}
