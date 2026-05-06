package com.oism.capitaltech.service;

import com.oism.capitaltech.dto.ForgotPasswordRequest;
import com.oism.capitaltech.dto.ResetPasswordRequest;
import com.oism.capitaltech.dto.VerifyResetCodeRequest;
import com.oism.capitaltech.entity.EmailConfig;
import com.oism.capitaltech.entity.PasswordResetToken;
import com.oism.capitaltech.entity.User;
import com.oism.capitaltech.repository.PasswordResetTokenRepository;
import com.oism.capitaltech.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);
    private static final int MAX_ATTEMPTS = 3;
    private static final int CODE_EXPIRY_MINUTES = 15;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailConfigService emailConfigService;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetService(UserRepository userRepository,
                                PasswordResetTokenRepository tokenRepository,
                                EmailConfigService emailConfigService,
                                PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.emailConfigService = emailConfigService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Envia código de 6 dígitos para o e-mail informado.
     * Sempre retorna 200 mesmo se o e-mail não existir (evita enumeração de usuários).
     */
    @Transactional
    public void sendResetCode(ForgotPasswordRequest request) {
        String email = request.email().trim().toLowerCase();

        // Verifica se o usuário existe — se não existir, apenas loga e retorna silenciosamente
        if (userRepository.findByEmail(email).isEmpty()) {
            log.info("Forgot-password solicitado para e-mail inexistente: {}", email);
            return;
        }

        // Remove tokens anteriores do mesmo e-mail
        tokenRepository.deleteAllByEmail(email);

        String code = generateCode();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(CODE_EXPIRY_MINUTES);
        tokenRepository.save(new PasswordResetToken(email, code, expiresAt));

        sendEmail(email, code);
        log.info("Código de reset enviado para: {}", email);
    }

    /**
     * Verifica se o código informado é válido.
     * Incrementa tentativas e lança exceção descritiva em caso de falha.
     */
    @Transactional
    public void verifyCode(VerifyResetCodeRequest request) {
        String email = request.email().trim().toLowerCase();
        PasswordResetToken token = findActiveToken(email);

        validateCodeAttempt(token, request.code());
    }

    /**
     * Redefine a senha após validar o código novamente.
     */
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String email = request.email().trim().toLowerCase();
        PasswordResetToken token = findActiveToken(email);

        validateCodeAttempt(token, request.code());

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Usuário não encontrado."));

        user.setSenha(passwordEncoder.encode(request.novaSenha()));
        userRepository.save(user);

        // Invalida o token após uso
        token.setUsed(true);
        tokenRepository.save(token);

        log.info("Senha redefinida com sucesso para: {}", email);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private PasswordResetToken findActiveToken(String email) {
        PasswordResetToken token = tokenRepository.findTopByEmailOrderByIdDesc(email)
                .orElseThrow(() -> new TokenNotFoundException("Nenhum código de redefinição encontrado para este e-mail."));

        if (token.isUsed()) {
            throw new TokenNotFoundException("Este código já foi utilizado. Solicite um novo.");
        }
        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new TokenExpiredException("Código expirado. Solicite um novo.");
        }
        return token;
    }

    private void validateCodeAttempt(PasswordResetToken token, String code) {
        if (!token.getCode().equals(code)) {
            int attempts = token.getAttempts() + 1;
            token.setAttempts(attempts);
            tokenRepository.save(token);

            int remaining = MAX_ATTEMPTS - attempts;
            if (remaining <= 0) {
                token.setUsed(true); // bloqueia o token
                tokenRepository.save(token);
                throw new MaxAttemptsExceededException("Número máximo de tentativas atingido. Solicite um novo código.");
            }
            throw new InvalidCodeException("Código incorreto. Você tem " + remaining + " tentativa(s) restante(s).", remaining);
        }
    }

    private String generateCode() {
        SecureRandom random = new SecureRandom();
        int number = random.nextInt(900_000) + 100_000; // 100000–999999
        return String.valueOf(number);
    }

    private void sendEmail(String to, String code) {
        EmailConfig cfg = emailConfigService.getActiveConfig();
        JavaMailSender sender = emailConfigService.buildMailSender();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(cfg.getFromName() + " <" + cfg.getFromAddress() + ">");
        message.setTo(to);
        message.setSubject("OISM Capital Tech — Código de redefinição de senha");
        message.setText(
                "Olá!\n\n" +
                "Recebemos uma solicitação para redefinir a senha da sua conta OISM Capital Tech.\n\n" +
                "Seu código de verificação é:\n\n" +
                "    " + code + "\n\n" +
                "Este código é válido por " + CODE_EXPIRY_MINUTES + " minutos.\n\n" +
                "Se você não solicitou a redefinição de senha, ignore este e-mail.\n\n" +
                "Equipe OISM Capital Tech"
        );
        sender.send(message);
    }

    // ── exceções específicas ──────────────────────────────────────────────────

    public static class TokenNotFoundException extends RuntimeException {
        public TokenNotFoundException(String message) { super(message); }
    }

    public static class TokenExpiredException extends RuntimeException {
        public TokenExpiredException(String message) { super(message); }
    }

    public static class InvalidCodeException extends RuntimeException {
        private final int remainingAttempts;
        public InvalidCodeException(String message, int remainingAttempts) {
            super(message);
            this.remainingAttempts = remainingAttempts;
        }
        public int getRemainingAttempts() { return remainingAttempts; }
    }

    public static class MaxAttemptsExceededException extends RuntimeException {
        public MaxAttemptsExceededException(String message) { super(message); }
    }
}
