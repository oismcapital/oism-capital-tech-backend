package com.oism.capitaltech.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "withdrawal_requests")
public class WithdrawalRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    /** PIX key provided by the user (CPF, email, phone or random key). */
    @Column(nullable = false, length = 255)
    private String pixKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private WithdrawalStatus status = WithdrawalStatus.PENDING;

    @Column(nullable = false)
    private Instant requestedAt = Instant.now();

    @Column
    private Instant processedAt;

    /** Optional failure reason if status = FAILED. */
    @Column(length = 500)
    private String failureReason;

    // ── Getters / Setters ────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getPixKey() { return pixKey; }
    public void setPixKey(String pixKey) { this.pixKey = pixKey; }

    public WithdrawalStatus getStatus() { return status; }
    public void setStatus(WithdrawalStatus status) { this.status = status; }

    public Instant getRequestedAt() { return requestedAt; }
    public void setRequestedAt(Instant requestedAt) { this.requestedAt = requestedAt; }

    public Instant getProcessedAt() { return processedAt; }
    public void setProcessedAt(Instant processedAt) { this.processedAt = processedAt; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
}
