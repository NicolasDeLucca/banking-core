package com.bankingcore.transaction.infrastructure.persistence;

import java.math.BigDecimal;
import java.time.Instant;

import com.bankingcore.transaction.domain.TransactionType;
import jakarta.persistence.*;

@Entity
@Table(name = "transactions")
public class TransactionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private Long accountId;

    @Column(updatable = false)
    private Long relatedAccountId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private TransactionType type;

    @Column(nullable = false, precision = 19, scale = 2, updatable = false)
    private BigDecimal amount;

    @Column(nullable = false, updatable = false)
    private Instant occurredAt;

    protected TransactionJpaEntity() {
        // required by JPA
    }

    public TransactionJpaEntity(Long id, Long accountId, Long relatedAccountId, TransactionType type, BigDecimal amount, Instant occurredAt) {
        this.id = id;
        this.accountId = accountId;
        this.relatedAccountId = relatedAccountId;
        this.type = type;
        this.amount = amount;
        this.occurredAt = occurredAt;
    }

    public Long getId() {
        return id;
    }

    public Long getAccountId() {
        return accountId;
    }

    public Long getRelatedAccountId() {
        return relatedAccountId;
    }

    public TransactionType getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
