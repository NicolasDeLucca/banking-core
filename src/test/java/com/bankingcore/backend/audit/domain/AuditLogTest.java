package com.bankingcore.backend.audit.domain;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuditLogTest {

    @Test
    void recordRequiresActionAndTargetType() {
        assertThatThrownBy(() -> AuditLog.record(1L, null, "USER", "1", null, Instant.now()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> AuditLog.record(1L, "LOGIN_SUCCESS", "", "1", null, Instant.now()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void recordAllowsNullActorForUnknownIdentity() {
        AuditLog auditLog = AuditLog.record(null, "LOGIN_FAILURE", "USER", null, "attempt", Instant.now());

        assertThat(auditLog.getActorUserId()).isNull();
        assertThat(auditLog.getId()).isNull();
        assertThat(auditLog.getAction()).isEqualTo("LOGIN_FAILURE");
    }
}
