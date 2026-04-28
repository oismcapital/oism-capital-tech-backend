package com.oism.capitaltech.service;

import com.oism.capitaltech.dto.FinanceSummaryResponse;
import com.oism.capitaltech.entity.Investment;
import com.oism.capitaltech.entity.InvestmentStatus;
import com.oism.capitaltech.entity.User;
import com.oism.capitaltech.entity.WalletTransaction;
import com.oism.capitaltech.entity.WalletTransactionType;
import com.oism.capitaltech.repository.InvestmentRepository;
import com.oism.capitaltech.repository.WalletTransactionRepository;
import com.oism.capitaltech.security.SecurityCurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneId;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class FinanceService {

    private static final BigDecimal DAILY_RATE =
            new BigDecimal("0.10").divide(new BigDecimal("30"), 10, RoundingMode.HALF_UP);

    private final UserService userService;
    private final SecurityCurrentUser securityCurrentUser;
    private final InvestmentRepository investmentRepository;
    private final WalletTransactionRepository walletTransactionRepository;

    public FinanceService(UserService userService,
                          SecurityCurrentUser securityCurrentUser,
                          InvestmentRepository investmentRepository,
                          WalletTransactionRepository walletTransactionRepository) {
        this.userService = userService;
        this.securityCurrentUser = securityCurrentUser;
        this.investmentRepository = investmentRepository;
        this.walletTransactionRepository = walletTransactionRepository;
    }

    @Transactional(readOnly = true)
    public FinanceSummaryResponse getSummary() {
        User user = userService.getByEmail(securityCurrentUser.email());

        List<Investment> active = investmentRepository
                .findByUserIdAndStatusOrderByContractedAtDesc(user.getId(), InvestmentStatus.ACTIVE);

        BigDecimal totalInvested = active.stream()
                .map(Investment::getPrincipal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(4, RoundingMode.HALF_UP);

        BigDecimal totalAccrued = active.stream()
                .map(Investment::getAccruedInterest)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(4, RoundingMode.HALF_UP);

        // Withdrawable interest = accrued interest from investments past D+15
        BigDecimal withdrawableInterest = active.stream()
                .filter(inv -> !LocalDate.now().isBefore(inv.getInterestWithdrawalDate()))
                .map(Investment::getAccruedInterest)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(4, RoundingMode.HALF_UP);

        // Withdrawable balance = wallet balance + withdrawable interest
        BigDecimal withdrawableBalance = user.getSaldo()
                .add(withdrawableInterest)
                .setScale(4, RoundingMode.HALF_UP);

        // Daily profit = sum of one day's interest across all active investments within earning window
        BigDecimal dailyProfit = active.stream()
                .filter(inv -> {
                    LocalDate contractDate = inv.getContractedAt().toLocalDate();
                    LocalDate earningEnd = contractDate.plusDays(29);
                    return !LocalDate.now().isAfter(earningEnd);
                })
                .map(inv -> inv.getPrincipal()
                        .multiply(DAILY_RATE)
                        .setScale(4, RoundingMode.HALF_UP))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(4, RoundingMode.HALF_UP);

        // Build performance chart from real accrued interest per investment (last 30 days)
        List<Double> points = buildPerformancePoints(user.getId(), active);

        return new FinanceSummaryResponse(
                user.getSaldo(),
                totalInvested,
                totalAccrued,
                withdrawableInterest,
                withdrawableBalance,
                dailyProfit,
                points,
                user.isValorEscondido()
        );
    }

    /**
     * Builds daily performance points from active investments.
     * Each point represents the cumulative accrued interest for that day.
     */
    private List<Double> buildPerformancePoints(Long userId, List<Investment> active) {
        if (active.isEmpty()) return List.of();

        // Find the earliest contract start date (up to 30 days ago)
        LocalDate today = LocalDate.now();
        LocalDate earliest = active.stream()
                .map(i -> i.getContractedAt().toLocalDate())
                .min(LocalDate::compareTo)
                .orElse(today);

        // Limit to last 30 days
        if (earliest.isBefore(today.minusDays(29))) {
            earliest = today.minusDays(29);
        }

        // Build day-by-day cumulative interest projection
        // For each day from earliest to today, sum the accrued interest
        // that would have been earned up to that day across all active investments
        Map<LocalDate, Double> dailyAccrual = new LinkedHashMap<>();
        LocalDate cursor = earliest;
        while (!cursor.isAfter(today)) {
            final LocalDate day = cursor;
            double dayTotal = active.stream()
                    .filter(inv -> !inv.getContractedAt().toLocalDate().isAfter(day))
                    .mapToDouble(inv -> {
                        // Days elapsed since contract start (capped at 30)
                        long daysElapsed = java.time.temporal.ChronoUnit.DAYS.between(
                                inv.getContractedAt().toLocalDate(), day);
                        daysElapsed = Math.min(daysElapsed, 30);
                        // Daily rate = 10% / 30
                        double dailyRate = 0.10 / 30.0;
                        return inv.getPrincipal().doubleValue() * dailyRate * daysElapsed;
                    })
                    .sum();
            dailyAccrual.put(day, dayTotal);
            cursor = cursor.plusDays(1);
        }

        return new ArrayList<>(dailyAccrual.values());
    }
}
