package com.oism.capitaltech.service;

import com.oism.capitaltech.dto.InvestmentResponse;
import com.oism.capitaltech.dto.PurchasePlanRequest;
import com.oism.capitaltech.entity.*;
import com.oism.capitaltech.repository.InvestmentRepository;
import com.oism.capitaltech.security.SecurityCurrentUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class InvestmentService {

    private static final Logger log = LoggerFactory.getLogger(InvestmentService.class);

    /** Taxa mensal de 10% sobre o valor nominal. */
    private static final BigDecimal MONTHLY_RATE = new BigDecimal("0.10");
    /** Juros diários = taxa mensal / 30. */
    private static final BigDecimal DAILY_RATE = MONTHLY_RATE.divide(new BigDecimal("30"), 10, RoundingMode.HALF_UP);

    private final InvestmentRepository investmentRepository;
    private final UserService userService;
    private final SecurityCurrentUser securityCurrentUser;

    public InvestmentService(InvestmentRepository investmentRepository,
                             UserService userService,
                             SecurityCurrentUser securityCurrentUser) {
        this.investmentRepository = investmentRepository;
        this.userService = userService;
        this.securityCurrentUser = securityCurrentUser;
    }

    // ── Contratar plano ──────────────────────────────────────────────────────

    @Transactional
    public InvestmentResponse purchase(PurchasePlanRequest request) {
        Plan plan = resolvePlan(request.planId());
        User user = userService.getByEmail(securityCurrentUser.email());

        // Débito atômico na Wallet com tipo PLAN_PURCHASE
        userService.debitBalance(user.getId(), plan.getAmount(),
                WalletTransactionType.PLAN_PURCHASE,
                Map.of("descricao", "Contratação plano " + plan.getDisplayName()));

        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();

        Investment inv = new Investment();
        inv.setUser(user);
        inv.setPlan(plan);
        inv.setPrincipal(plan.getAmount().setScale(4, RoundingMode.HALF_UP));
        inv.setAccruedInterest(BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP));
        inv.setStatus(InvestmentStatus.ACTIVE);
        inv.setContractedAt(now);
        inv.setInterestWithdrawalDate(today.plusDays(15));
        inv.setMaturityDate(today.plusDays(35));

        investmentRepository.save(inv);

        log.info("Plano contratado: userId={} plan={} principal={} maturity={}",
                user.getId(), plan.name(), plan.getAmount(), inv.getMaturityDate());

        return InvestmentResponse.fromEntity(inv);
    }

    // ── Listar investimentos ─────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<InvestmentResponse> listAll() {
        User user = userService.getByEmail(securityCurrentUser.email());
        return investmentRepository
                .findByUserIdOrderByContractedAtDesc(user.getId())
                .stream()
                .map(InvestmentResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InvestmentResponse> listActive() {
        User user = userService.getByEmail(securityCurrentUser.email());
        return investmentRepository
                .findByUserIdAndStatusOrderByContractedAtDesc(user.getId(), InvestmentStatus.ACTIVE)
                .stream()
                .map(InvestmentResponse::fromEntity)
                .toList();
    }

    // ── Resgatar lucro (D+15) ────────────────────────────────────────────────

    @Transactional
    public InvestmentResponse withdrawInterest(Long investmentId) {
        User user = userService.getByEmail(securityCurrentUser.email());

        Investment inv = investmentRepository.findByIdWithLock(investmentId)
                .orElseThrow(() -> new IllegalArgumentException("Investimento não encontrado: " + investmentId));

        if (!inv.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Investimento não pertence ao usuário");
        }
        if (inv.getStatus() != InvestmentStatus.ACTIVE) {
            throw new IllegalStateException("Investimento não está ativo");
        }
        if (LocalDate.now().isBefore(inv.getInterestWithdrawalDate())) {
            throw new IllegalStateException("Lucro ainda em carência. Disponível a partir de " + inv.getInterestWithdrawalDate());
        }

        BigDecimal interest = inv.getAccruedInterest();
        if (interest.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Nenhum lucro disponível para resgate");
        }

        // Zera os juros acumulados e credita na Wallet
        inv.setAccruedInterest(BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP));
        investmentRepository.save(inv);

        userService.creditBalance(user.getId(), interest,
                WalletTransactionType.INTEREST_WITHDRAWAL,
                Map.of("investmentId", String.valueOf(investmentId),
                        "plan", inv.getPlan().name()));

        log.info("Lucro resgatado: userId={} investmentId={} amount={}", user.getId(), investmentId, interest);

        return InvestmentResponse.fromEntity(inv);
    }

    // ── Accrual diário (chamado pelo robô) ───────────────────────────────────

    @Transactional
    public void processAccrual() {
        LocalDate today = LocalDate.now();
        // Só calcula juros para contratos dentro dos primeiros 30 dias
        LocalDateTime cutoff = today.minusDays(30).atStartOfDay();

        List<Investment> toProcess = investmentRepository.findActiveForAccrual(today, cutoff);

        for (Investment inv : toProcess) {
            BigDecimal dailyInterest = inv.getPrincipal()
                    .multiply(DAILY_RATE)
                    .setScale(4, RoundingMode.HALF_UP);

            inv.setAccruedInterest(inv.getAccruedInterest().add(dailyInterest));
            inv.setLastAccrualDate(today);
            investmentRepository.save(inv);

            log.info("Accrual: investmentId={} userId={} dailyInterest={} totalAccrued={}",
                    inv.getId(), inv.getUser().getId(), dailyInterest, inv.getAccruedInterest());
        }

        log.info("Accrual diário concluído: {} contratos processados", toProcess.size());
    }

    // ── Maturação automática (D+35, chamado pelo robô) ───────────────────────

    @Transactional
    public void processMaturities() {
        LocalDate today = LocalDate.now();
        List<Investment> matured = investmentRepository.findMaturedContracts(today);

        for (Investment inv : matured) {
            BigDecimal total = inv.getPrincipal().add(inv.getAccruedInterest())
                    .setScale(4, RoundingMode.HALF_UP);

            inv.setStatus(InvestmentStatus.MATURED);
            investmentRepository.save(inv);

            userService.creditBalance(inv.getUser().getId(), total,
                    WalletTransactionType.PLAN_MATURITY,
                    Map.of("investmentId", String.valueOf(inv.getId()),
                            "plan", inv.getPlan().name(),
                            "principal", inv.getPrincipal().toPlainString(),
                            "interest", inv.getAccruedInterest().toPlainString()));

            log.info("Plano maturado: investmentId={} userId={} total={}", inv.getId(), inv.getUser().getId(), total);
        }

        log.info("Maturações processadas: {} contratos encerrados", matured.size());
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Plan resolvePlan(String planId) {
        try {
            return Plan.valueOf(planId.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Plano não encontrado: " + planId);
        }
    }
}
