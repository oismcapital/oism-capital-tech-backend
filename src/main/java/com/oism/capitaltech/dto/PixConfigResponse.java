package com.oism.capitaltech.dto;

import com.oism.capitaltech.entity.PixConfig;

public record PixConfigResponse(
        Long id,
        String chavePix,
        String nomeRecebedor,
        String cidade,
        boolean ativa
) {
    public static PixConfigResponse fromEntity(PixConfig e) {
        return new PixConfigResponse(e.getId(), e.getChavePix(), e.getNomeRecebedor(), e.getCidade(), e.isAtiva());
    }
}
