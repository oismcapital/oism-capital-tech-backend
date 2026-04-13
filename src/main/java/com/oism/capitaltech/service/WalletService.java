package com.oism.capitaltech.service;

import com.oism.capitaltech.dto.UserResponse;
import com.oism.capitaltech.dto.WalletAmountRequest;
import com.oism.capitaltech.dto.WalletPreferencesRequest;
import com.oism.capitaltech.dto.WalletTransactionResponse;
import com.oism.capitaltech.entity.User;
import com.oism.capitaltech.security.SecurityCurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class WalletService {

    private final UserService userService;
    private final SecurityCurrentUser securityCurrentUser;

    public WalletService(UserService userService, SecurityCurrentUser securityCurrentUser) {
        this.userService = userService;
        this.securityCurrentUser = securityCurrentUser;
    }

    @Transactional(readOnly = true)
    public UserResponse me() {
        User user = userService.getByEmail(securityCurrentUser.email());
        return UserResponse.fromEntity(user);
    }

    @Transactional
    public UserResponse deposit(WalletAmountRequest request) {
        User user = userService.getByEmail(securityCurrentUser.email());
        userService.creditBalance(
                user.getId(),
                request.valor(),
                com.oism.capitaltech.entity.WalletTransactionType.DEPOSIT,
                Map.of("descricao", request.descricao())
        );
        return UserResponse.fromEntity(userService.getById(user.getId()));
    }

    @Transactional
    public UserResponse withdraw(WalletAmountRequest request) {
        User user = userService.getByEmail(securityCurrentUser.email());
        userService.debitBalance(user.getId(), request.valor(), Map.of("descricao", request.descricao()));
        return UserResponse.fromEntity(userService.getById(user.getId()));
    }

    @Transactional(readOnly = true)
    public List<WalletTransactionResponse> statement(int limit) {
        User user = userService.getByEmail(securityCurrentUser.email());
        return userService.latestTransactions(user.getId(), limit).stream()
                .map(WalletTransactionResponse::fromEntity)
                .toList();
    }

    @Transactional
    public void updatePreferences(WalletPreferencesRequest request) {
        User user = userService.getByEmail(securityCurrentUser.email());
        user.setValorEscondido(request.valorEscondido());
        userService.save(user);
    }
}
