package com.oism.capitaltech.service;

import com.oism.capitaltech.dto.EmailConfigRequest;
import com.oism.capitaltech.dto.EmailConfigResponse;
import com.oism.capitaltech.entity.EmailConfig;
import com.oism.capitaltech.repository.EmailConfigRepository;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Properties;

@Service
public class EmailConfigService {

    private final EmailConfigRepository repository;

    public EmailConfigService(EmailConfigRepository repository) {
        this.repository = repository;
    }

    /** Retorna a configuração ativa ou lança exceção se não houver nenhuma. */
    public EmailConfig getActiveConfig() {
        return repository.findFirstByAtivaTrue()
                .orElseThrow(() -> new EmailConfigNotFoundException(
                        "Nenhuma configuração de e-mail ativa encontrada. " +
                        "Configure o servidor SMTP em /api/v1/admin/email-config."));
    }

    /** Constrói um JavaMailSender com as configurações do banco. */
    public JavaMailSender buildMailSender() {
        EmailConfig cfg = getActiveConfig();

        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(cfg.getHost());
        sender.setPort(cfg.getPort());
        sender.setUsername(cfg.getUsername());
        sender.setPassword(cfg.getPassword());

        Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", String.valueOf(cfg.isStarttls()));
        props.put("mail.smtp.starttls.required", String.valueOf(cfg.isStarttls()));
        props.put("mail.debug", "false");

        return sender;
    }

    /** Salva ou substitui a configuração ativa. */
    @Transactional
    public EmailConfigResponse upsert(EmailConfigRequest request) {
        // Desativa todas as configurações anteriores
        repository.findAll().forEach(c -> {
            c.setAtiva(false);
            repository.save(c);
        });

        EmailConfig config = new EmailConfig();
        config.setHost(request.host());
        config.setPort(request.port());
        config.setUsername(request.username());
        config.setPassword(request.password());
        config.setFromAddress(request.fromAddress());
        config.setFromName(request.fromName());
        config.setStarttls(request.starttls());
        config.setAtiva(true);

        return EmailConfigResponse.fromEntity(repository.save(config));
    }

    /** Retorna a configuração ativa sem expor a senha. */
    public EmailConfigResponse getActiveResponse() {
        return EmailConfigResponse.fromEntity(getActiveConfig());
    }

    // ── exceção específica ────────────────────────────────────────────────────

    public static class EmailConfigNotFoundException extends RuntimeException {
        public EmailConfigNotFoundException(String message) { super(message); }
    }
}
