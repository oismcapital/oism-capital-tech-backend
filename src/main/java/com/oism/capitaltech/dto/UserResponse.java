package com.oism.capitaltech.dto;

import com.oism.capitaltech.entity.User;

import java.math.BigDecimal;

public record UserResponse(
        Long id,
        String nome,
        String email,
        BigDecimal saldo,
        BigDecimal lucroHoje,
        String historicoRendimentoJSONB,
        boolean valorEscondido
) {
    public static UserResponse fromEntity(User user) {
        return new UserResponse(
                user.getId(),
                user.getNome(),
                user.getEmail(),
                user.getSaldo(),
                user.getLucroHoje(),
                user.getHistoricoRendimentoJSONB(),
                user.isValorEscondido()
        );
    }
}
