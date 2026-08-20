package com.bankingcore.transaction.infrastructure.persistence;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.bankingcore.shared.paging.PageRequest;
import com.bankingcore.transaction.domain.Transaction;
import com.bankingcore.transaction.domain.TransactionRepository;

/** Transactions are append-only, so save() is always a plain insert (no update-in-place needed). */
@Repository
public class JpaTransactionRepositoryAdapter implements TransactionRepository {

    private final TransactionJpaRepository jpaRepository;
    private final TransactionMapper mapper;

    public JpaTransactionRepositoryAdapter(TransactionJpaRepository jpaRepository, TransactionMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Transaction save(Transaction transaction) {
        TransactionJpaEntity saved = jpaRepository.save(mapper.toJpaEntity(transaction));
        return mapper.toDomain(saved);
    }

    @Override
    public List<Transaction> findAllByAccountId(Long accountId, PageRequest pageRequest) {
        Pageable pageable = Pageable.ofSize(pageRequest.size()).withPage(pageRequest.page());
        return jpaRepository.findAllByAccountIdOrderByOccurredAtDesc(accountId, pageable).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
