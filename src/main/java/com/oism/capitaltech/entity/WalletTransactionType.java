package com.oism.capitaltech.entity;

public enum WalletTransactionType {
    DEPOSIT,
    WITHDRAW,
    PIX_CREDIT,
    YIELD,
    /** Débito na Wallet ao contratar um plano. */
    PLAN_PURCHASE,
    /** Crédito na Wallet ao resgatar lucro do plano (D+15). */
    INTEREST_WITHDRAWAL,
    /** Crédito na Wallet ao encerrar plano (D+35): principal + juros. */
    PLAN_MATURITY
}

