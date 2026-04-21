package com.oism.capitaltech.dto;

import java.math.BigDecimal;

public record DepositPixResponse(
        String transactionId,
        BigDecimal amount,
        String copyAndPaste,
        String qrCodeBase64,
        String status,
        String expiresAt
) {}
