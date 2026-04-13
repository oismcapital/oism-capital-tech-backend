package com.oism.capitaltech.service;

import com.oism.capitaltech.dto.RegisterRequest;
import com.oism.capitaltech.dto.UserResponse;
import com.oism.capitaltech.entity.User;
import com.oism.capitaltech.entity.WalletTransaction;
import com.oism.capitaltech.entity.WalletTransactionType;
import com.oism.capitaltech.repository.UserRepository;
import com.oism.capitaltech.repository.WalletTransactionRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final WalletTransactionRepository walletTransactionRepository;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       WalletTransactionRepository walletTransactionRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.walletTransactionRepository = walletTransactionRepository;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email ja cadastrado");
        }

        User user = new User();
        user.setNome(request.nome());
        user.setEmail(request.email());
        user.setSenha(passwordEncoder.encode(request.senha()));
        user.setSaldo(request.saldo().setScale(4, RoundingMode.HALF_UP));
        user.setLucroHoje(BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP));

        return UserResponse.fromEntity(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userRepository.findAll().stream()
                .map(UserResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario nao encontrado"));
    }

    @Transactional
    public void creditBalance(Long userId, BigDecimal amount) {
        creditBalance(userId, amount, WalletTransactionType.DEPOSIT, Map.of());
    }

    @Transactional
    public void creditBalance(Long userId,
                              BigDecimal amount,
                              WalletTransactionType type,
                              Map<String, Object> metadata) {
        User user = getById(userId);
        BigDecimal before = user.getSaldo().setScale(4, RoundingMode.HALF_UP);
        BigDecimal after = before.add(amount).setScale(4, RoundingMode.HALF_UP);
        user.setSaldo(after);
        userRepository.save(user);

        WalletTransaction t = new WalletTransaction();
        t.setUser(user);
        t.setType(type);
        t.setAmount(amount.setScale(4, RoundingMode.HALF_UP));
        t.setBalanceBefore(before);
        t.setBalanceAfter(after);
        t.setMetadataJson(toJson(metadata));
        walletTransactionRepository.save(t);
    }

    @Transactional
    public void debitBalance(Long userId, BigDecimal amount, Map<String, Object> metadata) {
        User user = getById(userId);
        BigDecimal before = user.getSaldo().setScale(4, RoundingMode.HALF_UP);
        BigDecimal debit = amount.setScale(4, RoundingMode.HALF_UP);

        if (before.compareTo(debit) < 0) {
            throw new IllegalArgumentException("Saldo insuficiente");
        }

        BigDecimal after = before.subtract(debit).setScale(4, RoundingMode.HALF_UP);
        user.setSaldo(after);
        userRepository.save(user);

        WalletTransaction t = new WalletTransaction();
        t.setUser(user);
        t.setType(WalletTransactionType.WITHDRAW);
        t.setAmount(debit);
        t.setBalanceBefore(before);
        t.setBalanceAfter(after);
        t.setMetadataJson(toJson(metadata));
        walletTransactionRepository.save(t);
    }

    @Transactional(readOnly = true)
    public List<WalletTransaction> latestTransactions(Long userId, int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 200);
        return walletTransactionRepository.findLatestByUserId(userId, PageRequest.of(0, safeLimit));
    }

    @Transactional
    public void applyDailyYield(BigDecimal multiplier) {
        userRepository.findAll().forEach(user -> {
            BigDecimal originalBalance = user.getSaldo();
            BigDecimal updatedBalance = originalBalance.multiply(multiplier).setScale(4, RoundingMode.HALF_UP);
            BigDecimal profit = updatedBalance.subtract(originalBalance).setScale(4, RoundingMode.HALF_UP);

            user.setSaldo(updatedBalance);
            user.setLucroHoje(profit);
            user.setHistoricoRendimentoJSONB(appendYieldHistory(user.getHistoricoRendimentoJSONB(), updatedBalance));

            WalletTransaction t = new WalletTransaction();
            t.setUser(user);
            t.setType(WalletTransactionType.YIELD);
            t.setAmount(profit);
            t.setBalanceBefore(originalBalance.setScale(4, RoundingMode.HALF_UP));
            t.setBalanceAfter(updatedBalance);
            t.setMetadataJson(toJson(Map.of("multiplier", multiplier.toPlainString())));
            walletTransactionRepository.save(t);
        });
    }

    private String appendYieldHistory(String currentHistory, BigDecimal newBalance) {
        String safeHistory = (currentHistory == null || currentHistory.isBlank()) ? "[]" : currentHistory;
        String today = java.time.LocalDate.now().toString();
        String entry = "{\"data\":\"" + today + "\",\"valor\":" + newBalance.toPlainString() + "}";

        if ("[]".equals(safeHistory)) {
            return "[" + entry + "]";
        }

        return safeHistory.substring(0, safeHistory.length() - 1) + "," + entry + "]";
    }

    @Transactional(readOnly = true)
    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario nao encontrado"));
    }

    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }

    private String toJson(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> e : metadata.entrySet()) {
            if (!first) sb.append(",");
            first = false;
            sb.append("\"").append(escape(e.getKey())).append("\":");
            Object v = e.getValue();
            if (v == null) {
                sb.append("null");
            } else {
                sb.append("\"").append(escape(String.valueOf(v))).append("\"");
            }
        }
        sb.append("}");
        return sb.toString();
    }

    private String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
