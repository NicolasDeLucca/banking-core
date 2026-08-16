package com.bankingcore.transaction.application;

import java.math.BigDecimal;
import java.time.Instant;

import com.bankingcore.transaction.domain.Transaction;
import com.bankingcore.transaction.domain.TransactionType;

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
