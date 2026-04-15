package com.oism.capitaltech.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PixConfigRequest(
        @NotBlank(message = "chavePix é obrigatória")
        String chavePix,

        @NotBlank(message = "nomeRecebedor é obrigatório")
        @Size(max = 25, message = "nomeRecebedor deve ter no máximo 25 caracteres")
        String nomeRecebedor,

        @NotBlank(message = "cidade é obrigatória")
        @Size(max = 15, message = "cidade deve ter no máximo 15 caracteres")
        String cidade
) {}
