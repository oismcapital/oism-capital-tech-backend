package com.oism.capitaltech.scheduler;

import com.oism.capitaltech.service.InvestmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RobotScheduler {

    private static final Logger log = LoggerFactory.getLogger(RobotScheduler.class);

    private final InvestmentService investmentService;

    public RobotScheduler(InvestmentService investmentService) {
        this.investmentService = investmentService;
    }

    /**
     * Executa diariamente à meia-noite:
     * 1. Calcula juros diários de todos os contratos ativos dentro dos 30 dias.
     * 2. Encerra contratos que atingiram D+35 e credita principal + juros na Wallet.
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void dailyProcessing() {
        log.info("=== Robô: início do processamento diário ===");
        try {
            investmentService.processAccrual();
        } catch (Exception e) {
            log.error("Erro no accrual diário", e);
        }
        try {
            investmentService.processMaturities();
        } catch (Exception e) {
            log.error("Erro no processamento de maturações", e);
        }
        log.info("=== Robô: processamento diário concluído ===");
    }
}
