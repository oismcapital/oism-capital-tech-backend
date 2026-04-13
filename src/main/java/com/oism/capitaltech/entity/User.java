package com.oism.capitaltech.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, unique = true, length = 180)
    private String email;

    @Column(nullable = false)
    private String senha;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal saldo = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal lucroHoje = BigDecimal.ZERO;

    // Coluna mantida com nome legado para compatibilidade com banco existente
    @Column(name = "historico_rendimento_josnb", nullable = false, length = 2000)
    private String historicoRendimentoJSONB = "[]";

    @Column(name = "valor_escondido", nullable = false)
    private boolean valorEscondido = false;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public BigDecimal getSaldo() { return saldo; }
    public void setSaldo(BigDecimal saldo) { this.saldo = saldo; }

    public BigDecimal getLucroHoje() { return lucroHoje; }
    public void setLucroHoje(BigDecimal lucroHoje) { this.lucroHoje = lucroHoje; }

    public String getHistoricoRendimentoJSONB() { return historicoRendimentoJSONB; }
    public void setHistoricoRendimentoJSONB(String historicoRendimentoJSONB) { this.historicoRendimentoJSONB = historicoRendimentoJSONB; }

    public boolean isValorEscondido() { return valorEscondido; }
    public void setValorEscondido(boolean valorEscondido) { this.valorEscondido = valorEscondido; }
}
