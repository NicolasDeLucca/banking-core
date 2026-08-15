package com.bankingcore.backend.transaction.application;

import java.math.BigDecimal;
import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bankingcore.backend.transaction.domain.Transaction;
import com.bankingcore.backend.transaction.domain.TransactionRepository;
import com.bankingcore.backend.transaction.domain.TransactionType;

/** Invoked by the account-movement event listener; never called directly from a controller. */
@Service
public class RecordTransactionUseCase {

    private final TransactionRepository transactionRepository;

    public RecordTransactionUseCase(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public TransactionResult execute(Long accountId, Long relatedAccountId, TransactionType type, BigDecimal amount, Instant occurredAt) {
        Transaction transaction = Transaction.record(accountId, relatedAccountId, type, amount, occurredAt);
        Transaction saved = transactionRepository.save(transaction);
        return TransactionResult.from(saved);
    }
}
