package com.bankingcore.transaction.infrastructure.persistence;

import org.springframework.stereotype.Component;

import com.bankingcore.transaction.domain.Transaction;

@Component
public class TransactionMapper {

    public TransactionJpaEntity toJpaEntity(Transaction transaction) {
        return new TransactionJpaEntity(
                transaction.getId(),
                transaction.getAccountId(),
                transaction.getRelatedAccountId(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getOccurredAt()
        );
    }

    public Transaction toDomain(TransactionJpaEntity entity) {
        return Transaction.reconstitute(
                entity.getId(),
                entity.getAccountId(),
                entity.getRelatedAccountId(),
                entity.getType(),
                entity.getAmount(),
                entity.getOccurredAt()
        );
    }
}
