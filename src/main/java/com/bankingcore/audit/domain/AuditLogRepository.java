package com.bankingcore.audit.domain;

import java.util.List;

import com.bankingcore.shared.paging.PageRequest;

public interface AuditLogRepository {

    AuditLog save(AuditLog auditLog);

    List<AuditLog> findAllOrderByOccurredAtDesc(PageRequest pageRequest);
}
