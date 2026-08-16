package com.bankingcore.audit.infrastructure.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

interface AuditLogJpaRepository extends JpaRepository<AuditLogJpaEntity, Long> {
    List<AuditLogJpaEntity> findAllByOrderByOccurredAtDesc();
}
