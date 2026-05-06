package com.oism.capitaltech.controller;

import com.oism.capitaltech.dto.EmailConfigRequest;
import com.oism.capitaltech.dto.EmailConfigResponse;
import com.oism.capitaltech.service.EmailConfigService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/email-config")
public class EmailConfigController {

    private final EmailConfigService emailConfigService;

    public EmailConfigController(EmailConfigService emailConfigService) {
        this.emailConfigService = emailConfigService;
    }

    /** Retorna a configuração SMTP ativa (sem expor a senha). */
    @GetMapping
    public ResponseEntity<EmailConfigResponse> get() {
        try {
            return ResponseEntity.ok(emailConfigService.getActiveResponse());
        } catch (EmailConfigService.EmailConfigNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Salva ou substitui a configuração SMTP ativa.
     * A senha é armazenada em texto simples no banco — use conexão segura (HTTPS) em produção.
     */
    @PostMapping
    public ResponseEntity<EmailConfigResponse> upsert(
            @Valid @RequestBody EmailConfigRequest request) {
        return ResponseEntity.ok(emailConfigService.upsert(request));
    }
}
