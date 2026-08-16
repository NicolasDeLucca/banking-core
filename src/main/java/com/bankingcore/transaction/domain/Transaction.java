package com.bankingcore.transaction.domain;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * An immutable ledger entry: once recorded, a transaction is a historical fact
 * and is never modified, only ever read (no behavior beyond its own creation).
 */
public final class Transaction {

    private final Long id;
    private final Long accountId;
    private final Long relatedAccountId;
    private final TransactionType type;
    private final BigDecimal amount;
    private final Instant occurredAt;

    private Transaction(Long id, Long accountId, Long relatedAccountId, TransactionType type, BigDecimal amount, Instant occurredAt) {
        this.id = id;
        this.accountId = accountId;
        this.relatedAccountId = relatedAccountId;
        this.type = type;
        this.amount = amount;
        this.occurredAt = occurredAt;
    }

    /**
     * @param relatedAccountId the counterpart account for TRANSFER_IN/TRANSFER_OUT, null for DEPOSIT/WITHDRAW
     * NOPMD - CyclomaticComplexity: four independent, sequential guard clauses;
     * splitting them into a separate method would move the complexity, not
     * reduce it, and would make the validation harder to read as one step.
     */
    public static Transaction record(Long accountId, Long relatedAccountId, TransactionType type, BigDecimal amount, Instant occurredAt) { // NOPMD
        if (accountId == null) {
            throw new IllegalArgumentException("accountId is required");
        }
        if (type == null) {
            throw new IllegalArgumentException("type is required");
        }
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
        if (occurredAt == null) {
            throw new IllegalArgumentException("occurredAt is required");
        }
        return new Transaction(null, accountId, relatedAccountId, type, amount, occurredAt);
    }

    public static Transaction reconstitute(Long id, Long accountId, Long relatedAccountId, TransactionType type, BigDecimal amount, Instant occurredAt) {
        return new Transaction(id, accountId, relatedAccountId, type, amount, occurredAt);
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
