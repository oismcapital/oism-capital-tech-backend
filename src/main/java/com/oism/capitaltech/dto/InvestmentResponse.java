package com.oism.capitaltech.dto;

import com.oism.capitaltech.entity.Investment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record InvestmentResponse(
        Long id,
        String planId,
        String planName,
        BigDecimal principal,
        BigDecimal accruedInterest,
        BigDecimal projectedTotalInterest,
        String status,
        LocalDateTime contractedAt,
        LocalDate interestWithdrawalDate,
        LocalDate maturityDate,
        boolean interestWithdrawable
) {
    private static final BigDecimal MONTHLY_RATE = new BigDecimal("0.10");
    private static final BigDecimal ACCRUAL_DAYS = new BigDecimal("30");

    public static InvestmentResponse fromEntity(Investment inv) {
        BigDecimal projected = inv.getPrincipal()
                .multiply(MONTHLY_RATE)
                .setScale(4, RoundingMode.HALF_UP);

        boolean withdrawable = inv.getStatus().name().equals("ACTIVE")
                && !LocalDate.now().isBefore(inv.getInterestWithdrawalDate())
                && inv.getAccruedInterest().compareTo(BigDecimal.ZERO) > 0;

        return new InvestmentResponse(
                inv.getId(),
                inv.getPlan().name(),
                inv.getPlan().getDisplayName(),
                inv.getPrincipal(),
                inv.getAccruedInterest(),
                projected,
                inv.getStatus().name(),
                inv.getContractedAt(),
                inv.getInterestWithdrawalDate(),
                inv.getMaturityDate(),
                withdrawable
        );
    }
}
