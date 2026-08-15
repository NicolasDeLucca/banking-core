package com.bankingcore.backend.transaction.application;

import java.math.BigDecimal;
import java.time.Instant;

import com.bankingcore.backend.transaction.domain.Transaction;
import com.bankingcore.backend.transaction.domain.TransactionType;

public record TransactionResult(
        Long id,
        Long accountId,
        Long relatedAccountId,
        TransactionType type,
        BigDecimal amount,
        Instant occurredAt
) {

    public static TransactionResult from(Transaction transaction) {
        return new TransactionResult(
                transaction.getId(),
                transaction.getAccountId(),
                transaction.getRelatedAccountId(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getOccurredAt()
        );
    }
}
