package com.oism.capitaltech.dto;

import java.math.BigDecimal;

public record PixQrCodeResponse(
        String transactionId,
        String qrCode,
        String qrCodeBase64,
        BigDecimal valor,
        String status
) {
}
