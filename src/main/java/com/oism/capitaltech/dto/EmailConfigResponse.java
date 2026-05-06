package com.oism.capitaltech.dto;

import com.oism.capitaltech.entity.EmailConfig;

public record EmailConfigResponse(
        Long id,
        String host,
        int port,
        String username,
        /** Senha nunca é retornada — apenas indica se está configurada */
        boolean senhaConfigurada,
        String fromAddress,
        String fromName,
        boolean starttls,
        boolean ativa
) {
    public static EmailConfigResponse fromEntity(EmailConfig e) {
        return new EmailConfigResponse(
                e.getId(),
                e.getHost(),
                e.getPort(),
                e.getUsername(),
                e.getPassword() != null && !e.getPassword().isBlank(),
                e.getFromAddress(),
                e.getFromName(),
                e.isStarttls(),
                e.isAtiva()
        );
    }
}
