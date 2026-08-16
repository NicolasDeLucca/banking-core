package com.bankingcore.audit.infrastructure.persistence;

import org.springframework.stereotype.Component;

import com.bankingcore.audit.domain.AuditLog;

@Component
public class AuditLogMapper {

    public AuditLogJpaEntity toJpaEntity(AuditLog auditLog) {
        return new AuditLogJpaEntity(
                auditLog.getId(),
                auditLog.getActorUserId(),
                auditLog.getAction(),
                auditLog.getTargetType(),
                auditLog.getTargetId(),
                auditLog.getDescription(),
                auditLog.getOccurredAt()
        );
    }

    public AuditLog toDomain(AuditLogJpaEntity entity) {
        return AuditLog.reconstitute(
                entity.getId(),
                entity.getActorUserId(),
                entity.getAction(),
                entity.getTargetType(),
                entity.getTargetId(),
                entity.getDescription(),
                entity.getOccurredAt()
        );
    }
}
