package com.bankingcore.auth.domain.event;

import java.time.Instant;

/** @param userId null when the attempted email does not match any user */
public record UserAuthenticationEvent(Long userId, String email, boolean successful, Instant occurredAt) {
}
