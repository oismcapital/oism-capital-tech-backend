package com.oism.capitaltech.dto;

import jakarta.validation.constraints.*;

public record EmailConfigRequest(
        @NotBlank String host,
        @Min(1) @Max(65535) int port,
        @NotBlank @Email String username,
        @NotBlank String password,
        @NotBlank @Email String fromAddress,
        @NotBlank String fromName,
        boolean starttls
) {}
