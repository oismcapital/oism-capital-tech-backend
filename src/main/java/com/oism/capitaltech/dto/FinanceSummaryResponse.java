package com.oism.capitaltech.dto;

import java.math.BigDecimal;
import java.util.List;

public record FinanceSummaryResponse(
        BigDecimal walletBalance,
        BigDecimal totalInvested,
        BigDecimal totalAccruedInterest,
        BigDecimal dailyProfit,
        List<Double> performancePoints,
        boolean valorEscondido
) {
    // Mantém compatibilidade com código legado que usa investedBalance
    public BigDecimal investedBalance() { return walletBalance; }
}
