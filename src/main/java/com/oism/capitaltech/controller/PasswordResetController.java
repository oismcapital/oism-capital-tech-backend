package com.oism.capitaltech.controller;

import com.oism.capitaltech.dto.ForgotPasswordRequest;
import com.oism.capitaltech.dto.ResetPasswordRequest;
import com.oism.capitaltech.dto.VerifyResetCodeRequest;
import com.oism.capitaltech.service.PasswordResetService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    /** Passo 1 — solicita envio do código por e-mail. */
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.sendResetCode(request);
        return ResponseEntity.ok(Map.of("message",
                "Se este e-mail estiver cadastrado, você receberá um código em breve."));
    }

    /** Passo 2 — valida o código sem ainda alterar a senha. */
    @PostMapping("/verify-reset-code")
    public ResponseEntity<Map<String, String>> verifyCode(
            @Valid @RequestBody VerifyResetCodeRequest request) {
        passwordResetService.verifyCode(request);
        return ResponseEntity.ok(Map.of("message", "Código válido."));
    }

    /** Passo 3 — redefine a senha (valida o código novamente por segurança). */
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request);
        return ResponseEntity.ok(Map.of("message", "Senha redefinida com sucesso."));
    }
}
