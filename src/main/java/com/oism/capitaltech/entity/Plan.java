package com.oism.capitaltech.entity;

import java.math.BigDecimal;

public enum Plan {

    START("Start", new BigDecimal("25.00"), "Plano inicial para novos investidores"),
    BASIC("Basic", new BigDecimal("50.00"), "Plano básico com rendimentos diários"),
    PLUS("Plus", new BigDecimal("100.00"), "Plano plus com rendimentos otimizados"),
    PRO("Pro", new BigDecimal("500.00"), "Plano profissional com máximo rendimento"),
    ELITE("Elite", new BigDecimal("1000.00"), "Plano elite com rendimento exclusivo");

    private final String displayName;
    private final BigDecimal amount;
    private final String description;

    Plan(String displayName, BigDecimal amount, String description) {
        this.displayName = displayName;
        this.amount = amount;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public BigDecimal getAmount() { return amount; }
    public String getDescription() { return description; }
}
