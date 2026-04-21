package com.oism.capitaltech.dto;

import jakarta.validation.constraints.NotBlank;

public record PurchasePlanRequest(
        @NotBlank String planId
) {}
