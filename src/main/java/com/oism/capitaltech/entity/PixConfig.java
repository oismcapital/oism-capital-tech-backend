package com.oism.capitaltech.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "pix_config")
public class PixConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 180)
    private String chavePix;

    @Column(nullable = false, length = 25)
    private String nomeRecebedor;

    @Column(nullable = false, length = 15)
    private String cidade;

    @Column(nullable = false)
    private boolean ativa = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getChavePix() { return chavePix; }
    public void setChavePix(String chavePix) { this.chavePix = chavePix; }

    public String getNomeRecebedor() { return nomeRecebedor; }
    public void setNomeRecebedor(String nomeRecebedor) { this.nomeRecebedor = nomeRecebedor; }

    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }

    public boolean isAtiva() { return ativa; }
    public void setAtiva(boolean ativa) { this.ativa = ativa; }
}
