package com.bankingcore.audit.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

interface AuditLogJpaRepository extends JpaRepository<AuditLogJpaEntity, Long> {
    Page<AuditLogJpaEntity> findAllByOrderByOccurredAtDesc(Pageable pageable);
}
