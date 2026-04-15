package com.oism.capitaltech.dto;

import java.math.BigDecimal;

public record GeneratePixResponse(
        String transactionId,
        String planId,
        String planName,
        BigDecimal amount,
        String copyAndPaste,
        String qrCodeBase64,
        String status,
        String expiresAt
) {}
