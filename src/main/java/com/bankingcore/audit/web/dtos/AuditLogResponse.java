package com.bankingcore.audit.web.dtos;

import java.time.Instant;

public record AuditLogResponse(
        Long id,
        Long actorUserId,
        String action,
        String targetType,
        String targetId,
        String description,
        Instant occurredAt
) {
}
