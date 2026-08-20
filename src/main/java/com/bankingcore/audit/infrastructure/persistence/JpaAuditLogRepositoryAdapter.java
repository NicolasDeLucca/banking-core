package com.bankingcore.audit.infrastructure.persistence;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.bankingcore.audit.domain.AuditLog;
import com.bankingcore.audit.domain.AuditLogRepository;
import com.bankingcore.shared.paging.PageRequest;

/** Audit logs are append-only, so save() is always a plain insert. */
@Repository
public class JpaAuditLogRepositoryAdapter implements AuditLogRepository {

    private final AuditLogJpaRepository jpaRepository;
    private final AuditLogMapper mapper;

    public JpaAuditLogRepositoryAdapter(AuditLogJpaRepository jpaRepository, AuditLogMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public AuditLog save(AuditLog auditLog) {
        AuditLogJpaEntity saved = jpaRepository.save(mapper.toJpaEntity(auditLog));
        return mapper.toDomain(saved);
    }

    @Override
    public List<AuditLog> findAllOrderByOccurredAtDesc(PageRequest pageRequest) {
        Pageable pageable = Pageable.ofSize(pageRequest.size()).withPage(pageRequest.page());
        return jpaRepository.findAllByOrderByOccurredAtDesc(pageable).stream().map(mapper::toDomain).toList();
    }
}
