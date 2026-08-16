package com.bankingcore.audit.infrastructure.persistence;

import java.time.Instant;

import jakarta.persistence.*;

@Entity
@Table(name = "audit_logs")
public class AuditLogJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(updatable = false)
    private Long actorUserId;

    @Column(nullable = false, updatable = false)
    private String action;

    @Column(nullable = false, updatable = false)
    private String targetType;

    @Column(updatable = false)
    private String targetId;

    @Column(updatable = false, length = 1000)
    private String description;

    @Column(nullable = false, updatable = false)
    private Instant occurredAt;

    protected AuditLogJpaEntity() {
        // required by JPA
    }

    public AuditLogJpaEntity(Long id, Long actorUserId, String action, String targetType, String targetId, String description, Instant occurredAt) {
        this.id = id;
        this.actorUserId = actorUserId;
        this.action = action;
        this.targetType = targetType;
        this.targetId = targetId;
        this.description = description;
        this.occurredAt = occurredAt;
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
