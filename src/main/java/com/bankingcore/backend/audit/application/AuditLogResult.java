package com.bankingcore.backend.audit.application;

import java.time.Instant;

import com.bankingcore.backend.audit.domain.AuditLog;

public record AuditLogResult(
        Long id,
        Long actorUserId,
        String action,
        String targetType,
        String targetId,
        String description,
        Instant occurredAt
) {

    public static AuditLogResult from(AuditLog auditLog) {
        return new AuditLogResult(
                auditLog.getId(),
                auditLog.getActorUserId(),
                auditLog.getAction(),
                auditLog.getTargetType(),
                auditLog.getTargetId(),
                auditLog.getDescription(),
                auditLog.getOccurredAt()
        );
    }
}
