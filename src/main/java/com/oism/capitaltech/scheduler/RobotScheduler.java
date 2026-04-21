package com.oism.capitaltech.scheduler;

import com.oism.capitaltech.entity.PixDeposit;
import com.oism.capitaltech.repository.PixDepositRepository;
import com.oism.capitaltech.service.InvestmentService;
import com.oism.capitaltech.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RobotScheduler {

    private static final Logger log = LoggerFactory.getLogger(RobotScheduler.class);

    private final InvestmentService investmentService;
    private final PaymentService paymentService;
    private final PixDepositRepository pixDepositRepository;

    public RobotScheduler(InvestmentService investmentService,
                          PaymentService paymentService,
                          PixDepositRepository pixDepositRepository) {
        this.investmentService = investmentService;
        this.paymentService = paymentService;
        this.pixDepositRepository = pixDepositRepository;
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

    /**
     * A cada 30 segundos: processa depósitos PIX que foram confirmados
     * externamente (ex: webhook do banco, confirmação manual no banco).
     */
    @Scheduled(fixedDelay = 30000)
    public void processCompletedDeposits() {
        List<PixDeposit> pending = pixDepositRepository.findCompletedWithoutProcessing();
        if (pending.isEmpty()) return;

        log.info("Processando {} depósito(s) confirmado(s) externamente", pending.size());
        for (PixDeposit deposit : pending) {
            try {
                paymentService.confirmPayment(deposit.getTransactionId());
                log.info("Depósito processado: transactionId={} amount={}",
                        deposit.getTransactionId(), deposit.getAmount());
            } catch (Exception e) {
                log.error("Erro ao processar depósito {}: {}", deposit.getTransactionId(), e.getMessage());
            }
        }
    }
}
