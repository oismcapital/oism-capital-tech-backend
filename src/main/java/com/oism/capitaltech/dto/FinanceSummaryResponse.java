package com.oism.capitaltech.dto;

import java.math.BigDecimal;
import java.util.List;

public record FinanceSummaryResponse(
        BigDecimal walletBalance,
        BigDecimal totalInvested,
        BigDecimal totalAccruedInterest,
        BigDecimal withdrawableInterest,
        BigDecimal withdrawableBalance,
        BigDecimal dailyProfit,
        List<Double> performancePoints,
        boolean valorEscondido
) {
    public BigDecimal investedBalance() { return walletBalance; }
}
