package com.bankingcore.audit.domain;

import java.time.Instant;

/**
 * An immutable "who did what, when" entry. Deliberately generic (a free-text
 * action code + target) instead of a rigid enum shared across modules: audit is
 * a sink that translates events from other modules, and shouldn't force those
 * modules to depend on audit's own vocabulary.
 */
public class AuditLog {

    private final Long id;
    private final Long actorUserId;
    private final String action;
    private final String targetType;
    private final String targetId;
    private final String description;
    private final Instant occurredAt;

    private AuditLog(Long id, Long actorUserId, String action, String targetType, String targetId, String description, Instant occurredAt) {
        this.id = id;
        this.actorUserId = actorUserId;
        this.action = action;
        this.targetType = targetType;
        this.targetId = targetId;
        this.description = description;
        this.occurredAt = occurredAt;
    }

    /** @param actorUserId nullable (e.g. a failed login attempt for an unknown email) */
    public static AuditLog record(Long actorUserId, String action, String targetType, String targetId, String description, Instant occurredAt) {
        if (action == null || action.isBlank()) {
            throw new IllegalArgumentException("action is required");
        }
        if (targetType == null || targetType.isBlank()) {
            throw new IllegalArgumentException("targetType is required");
        }
        if (occurredAt == null) {
            throw new IllegalArgumentException("occurredAt is required");
        }
        return new AuditLog(null, actorUserId, action, targetType, targetId, description, occurredAt);
    }

    public static AuditLog reconstitute(Long id, Long actorUserId, String action, String targetType, String targetId, String description, Instant occurredAt) {
        return new AuditLog(id, actorUserId, action, targetType, targetId, description, occurredAt);
    }

    public Long getId() {
        return id;
    }

    public Long getActorUserId() {
        return actorUserId;
    }

    public String getAction() {
        return action;
    }

    public String getTargetType() {
        return targetType;
    }

    public String getTargetId() {
        return targetId;
    }

    public String getDescription() {
        return description;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
