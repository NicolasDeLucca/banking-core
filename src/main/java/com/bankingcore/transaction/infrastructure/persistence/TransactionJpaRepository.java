package com.bankingcore.transaction.infrastructure.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

interface TransactionJpaRepository extends JpaRepository<TransactionJpaEntity, Long> {
    List<TransactionJpaEntity> findAllByAccountIdOrderByOccurredAtDesc(Long accountId);
}
