package com.bankingcore.audit.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bankingcore.audit.domain.AuditLogRepository;

/** No role check here: reachable only through the ADMIN-protected /api/admin/audit-logs route. */
@Service
public class ListAuditLogsUseCase {

    private final AuditLogRepository auditLogRepository;

    public ListAuditLogsUseCase(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(readOnly = true)
    public List<AuditLogResult> execute() {
        return auditLogRepository.findAllOrderByOccurredAtDesc().stream().map(AuditLogResult::from).toList();
    }
}
