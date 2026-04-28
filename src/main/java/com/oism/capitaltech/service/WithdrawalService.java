package com.oism.capitaltech.service;

import com.oism.capitaltech.dto.WithdrawalRequestDto;
import com.oism.capitaltech.dto.WithdrawalResponse;
import com.oism.capitaltech.entity.Investment;
import com.oism.capitaltech.entity.InvestmentStatus;
import com.oism.capitaltech.entity.User;
import com.oism.capitaltech.entity.WalletTransactionType;
import com.oism.capitaltech.entity.WithdrawalRequest;
import com.oism.capitaltech.entity.WithdrawalStatus;
import com.oism.capitaltech.repository.InvestmentRepository;
import com.oism.capitaltech.repository.WithdrawalRequestRepository;
import com.oism.capitaltech.security.SecurityCurrentUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class WithdrawalService {

    private static final Logger log = LoggerFactory.getLogger(WithdrawalService.class);

    private final WithdrawalRequestRepository withdrawalRequestRepository;
    private final UserService userService;
    private final SecurityCurrentUser securityCurrentUser;
    private final InvestmentRepository investmentRepository;

    public WithdrawalService(WithdrawalRequestRepository withdrawalRequestRepository,
                             UserService userService,
                             SecurityCurrentUser securityCurrentUser,
                             InvestmentRepository investmentRepository) {
        this.withdrawalRequestRepository = withdrawalRequestRepository;
        this.userService = userService;
        this.securityCurrentUser = securityCurrentUser;
        this.investmentRepository = investmentRepository;
    }

    /**
     * Creates a withdrawal request.
     * If wallet balance is insufficient but withdrawable interest covers the gap,
     * automatically credits the eligible interest to the wallet first.
     */
    @Transactional
    public WithdrawalResponse requestWithdrawal(WithdrawalRequestDto dto) {
        User user = userService.getByEmail(securityCurrentUser.email());
        BigDecimal amount = dto.amount().setScale(4, RoundingMode.HALF_UP);

        // If wallet balance is not enough, auto-credit eligible interest first
        BigDecimal walletBalance = userService.getById(user.getId()).getSaldo();
        if (walletBalance.compareTo(amount) < 0) {
            BigDecimal deficit = amount.subtract(walletBalance);
            autoWithdrawEligibleInterest(user.getId(), deficit);
        }

        // Debit wallet atomically — throws if still insufficient
        userService.debitBalance(
                user.getId(),
                amount,
                WalletTransactionType.WITHDRAW,
                Map.of(
                        "pixKey", dto.pixKey(),
                        "description", "PIX withdrawal to " + dto.pixKey()
                )
        );

        WithdrawalRequest request = new WithdrawalRequest();
        request.setUser(user);
        request.setAmount(amount);
        request.setPixKey(dto.pixKey());
        request.setStatus(WithdrawalStatus.PENDING);

        withdrawalRequestRepository.save(request);

        log.info("Withdrawal requested: userId={} amount={} pixKey={}",
                user.getId(), amount, dto.pixKey());

        return WithdrawalResponse.fromEntity(request);
    }

    /**
     * Auto-credits eligible interest (D+15 passed) to the wallet to cover a withdrawal deficit.
     */
    private void autoWithdrawEligibleInterest(Long userId, BigDecimal deficit) {
        List<Investment> eligible = investmentRepository
                .findByUserIdAndStatusOrderByContractedAtDesc(userId, InvestmentStatus.ACTIVE)
                .stream()
                .filter(inv -> !LocalDate.now().isBefore(inv.getInterestWithdrawalDate()))
                .filter(inv -> inv.getAccruedInterest().compareTo(BigDecimal.ZERO) > 0)
                .toList();

        BigDecimal remaining = deficit;
        for (Investment inv : eligible) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;

            BigDecimal toCredit = inv.getAccruedInterest().min(remaining);
            inv.setAccruedInterest(inv.getAccruedInterest().subtract(toCredit).setScale(4, RoundingMode.HALF_UP));
            investmentRepository.save(inv);

            userService.creditBalance(userId, toCredit,
                    WalletTransactionType.INTEREST_WITHDRAWAL,
                    Map.of("investmentId", String.valueOf(inv.getId()),
                            "plan", inv.getPlan().name(),
                            "reason", "auto-credit for withdrawal"));

            remaining = remaining.subtract(toCredit);
            log.info("Auto-credited interest: investmentId={} amount={}", inv.getId(), toCredit);
        }
    }

    @Transactional(readOnly = true)
    public List<WithdrawalResponse> listMyWithdrawals() {
        User user = userService.getByEmail(securityCurrentUser.email());
        return withdrawalRequestRepository
                .findByUserIdOrderByRequestedAtDesc(user.getId())
                .stream()
                .map(WithdrawalResponse::fromEntity)
                .toList();
    }

    /** Called by admin/integration to mark a withdrawal as completed. */
    @Transactional
    public WithdrawalResponse complete(Long withdrawalId) {
        WithdrawalRequest request = findById(withdrawalId);
        request.setStatus(WithdrawalStatus.COMPLETED);
        request.setProcessedAt(Instant.now());
        withdrawalRequestRepository.save(request);
        log.info("Withdrawal completed: id={}", withdrawalId);
        return WithdrawalResponse.fromEntity(request);
    }

    /** Called by admin/integration to mark a withdrawal as failed and refund the balance. */
    @Transactional
    public WithdrawalResponse fail(Long withdrawalId, String reason) {
        WithdrawalRequest request = findById(withdrawalId);
        if (request.getStatus() != WithdrawalStatus.PENDING &&
                request.getStatus() != WithdrawalStatus.PROCESSING) {
            throw new IllegalStateException("Cannot fail a withdrawal with status: " + request.getStatus());
        }

        // Refund the balance
        userService.creditBalance(
                request.getUser().getId(),
                request.getAmount(),
                WalletTransactionType.DEPOSIT,
                Map.of("description", "Refund for failed withdrawal #" + withdrawalId)
        );

        request.setStatus(WithdrawalStatus.FAILED);
        request.setProcessedAt(Instant.now());
        request.setFailureReason(reason);
        withdrawalRequestRepository.save(request);

        log.info("Withdrawal failed: id={} reason={}", withdrawalId, reason);
        return WithdrawalResponse.fromEntity(request);
    }

    private WithdrawalRequest findById(Long id) {
        return withdrawalRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Withdrawal not found: " + id));
    }
}
