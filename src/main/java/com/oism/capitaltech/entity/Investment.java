package com.oism.capitaltech.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "investments")
public class Investment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Plan plan;

    /** Valor nominal alocado (principal). */
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal principal;

    /** Juros acumulados até o momento (calculados diariamente pelo robô). */
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal accruedInterest = BigDecimal.ZERO;

    /** Total de juros já resgatados pelo usuário neste contrato. */
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal withdrawnInterest = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private InvestmentStatus status = InvestmentStatus.ACTIVE;

    /** Data/hora de contratação. */
    @Column(nullable = false)
    private LocalDateTime contractedAt;

    /** D+15 — a partir desta data o lucro pode ser resgatado. */
    @Column(nullable = false)
    private LocalDate interestWithdrawalDate;

    /** D+35 — data de maturação/encerramento do plano. */
    @Column(nullable = false)
    private LocalDate maturityDate;

    /** Último dia em que o robô calculou juros para este contrato. */
    @Column
    private LocalDate lastAccrualDate;

    /** Versão para lock otimista. */
    @Version
    private Long version;

    // ── Getters / Setters ────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Plan getPlan() { return plan; }
    public void setPlan(Plan plan) { this.plan = plan; }

    public BigDecimal getPrincipal() { return principal; }
    public void setPrincipal(BigDecimal principal) { this.principal = principal; }

    public BigDecimal getAccruedInterest() { return accruedInterest; }
    public void setAccruedInterest(BigDecimal accruedInterest) { this.accruedInterest = accruedInterest; }

    public BigDecimal getWithdrawnInterest() { 
        return withdrawnInterest != null ? withdrawnInterest : BigDecimal.ZERO; 
    }
    public void setWithdrawnInterest(BigDecimal withdrawnInterest) { 
        this.withdrawnInterest = withdrawnInterest != null ? withdrawnInterest : BigDecimal.ZERO; 
    }

    public InvestmentStatus getStatus() { return status; }
    public void setStatus(InvestmentStatus status) { this.status = status; }

    public LocalDateTime getContractedAt() { return contractedAt; }
    public void setContractedAt(LocalDateTime contractedAt) { this.contractedAt = contractedAt; }

    public LocalDate getInterestWithdrawalDate() { return interestWithdrawalDate; }
    public void setInterestWithdrawalDate(LocalDate interestWithdrawalDate) { this.interestWithdrawalDate = interestWithdrawalDate; }

    public LocalDate getMaturityDate() { return maturityDate; }
    public void setMaturityDate(LocalDate maturityDate) { this.maturityDate = maturityDate; }

    public LocalDate getLastAccrualDate() { return lastAccrualDate; }
    public void setLastAccrualDate(LocalDate lastAccrualDate) { this.lastAccrualDate = lastAccrualDate; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
