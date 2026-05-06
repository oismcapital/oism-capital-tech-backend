package com.oism.capitaltech.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "email_config")
public class EmailConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Servidor SMTP (ex: smtp.gmail.com) */
    @Column(nullable = false, length = 255)
    private String host;

    /** Porta SMTP (ex: 587) */
    @Column(nullable = false)
    private int port = 587;

    /** Usuário/login do SMTP */
    @Column(nullable = false, length = 255)
    private String username;

    /** Senha ou App Password do SMTP */
    @Column(nullable = false, length = 255)
    private String password;

    /** Endereço exibido no campo "De:" dos e-mails */
    @Column(nullable = false, length = 255)
    private String fromAddress;

    /** Nome exibido no campo "De:" dos e-mails */
    @Column(nullable = false, length = 120)
    private String fromName = "OISM Capital Tech";

    /** Habilitar STARTTLS */
    @Column(nullable = false)
    private boolean starttls = true;

    /** Registro ativo (apenas um deve estar ativo por vez) */
    @Column(nullable = false)
    private boolean ativa = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }

    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFromAddress() { return fromAddress; }
    public void setFromAddress(String fromAddress) { this.fromAddress = fromAddress; }

    public String getFromName() { return fromName; }
    public void setFromName(String fromName) { this.fromName = fromName; }

    public boolean isStarttls() { return starttls; }
    public void setStarttls(boolean starttls) { this.starttls = starttls; }

    public boolean isAtiva() { return ativa; }
    public void setAtiva(boolean ativa) { this.ativa = ativa; }
}
