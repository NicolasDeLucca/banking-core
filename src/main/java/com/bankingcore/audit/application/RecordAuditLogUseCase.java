package com.bankingcore.audit.application;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.bankingcore.audit.domain.AuditLog;
import com.bankingcore.audit.domain.AuditLogRepository;

/**
 * Invoked by event listeners; never called directly from a controller.
 *
 * REQUIRES_NEW on purpose: some callers (the login-attempt listener) invoke this
 * from a TransactionSynchronization#afterCompletion callback, where the outer
 * transaction's thread-bound resources may not be fully cleared yet. Joining that
 * transaction (the REQUIRED default) can silently bind to an already-finalizing
 * resource and never actually commit. REQUIRES_NEW always starts a genuinely
 * independent transaction, which also fits audit logging conceptually: it's a
 * best-effort trail that shouldn't be coupled to the outcome of the operation it
 * describes (unlike transaction.application.RecordTransactionUseCase, which must
 * stay atomic with the balance change it records).
 */
@Service
public class RecordAuditLogUseCase {

    private final AuditLogRepository auditLogRepository;

    public RecordAuditLogUseCase(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AuditLogResult execute(Long actorUserId, String action, String targetType, String targetId, String description, Instant occurredAt) {
        AuditLog auditLog = AuditLog.record(actorUserId, action, targetType, targetId, description, occurredAt);
        AuditLog saved = auditLogRepository.save(auditLog);
        return AuditLogResult.from(saved);
    }
}
