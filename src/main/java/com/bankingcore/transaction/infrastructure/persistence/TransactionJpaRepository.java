package com.bankingcore.transaction.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

interface TransactionJpaRepository extends JpaRepository<TransactionJpaEntity, Long> {
    Page<TransactionJpaEntity> findAllByAccountIdOrderByOccurredAtDesc(Long accountId, Pageable pageable);
}
