package com.oism.capitaltech.dto;

import java.math.BigDecimal;
import java.util.List;

public record FinanceSummaryResponse(
        BigDecimal investedBalance,
        BigDecimal dailyProfit,
        List<Double> performancePoints,
        boolean valorEscondido
) {}
