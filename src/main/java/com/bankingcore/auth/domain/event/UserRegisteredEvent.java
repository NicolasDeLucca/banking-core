package com.bankingcore.auth.domain.event;

import java.time.Instant;

public record UserRegisteredEvent(Long userId, String email, Instant occurredAt) {
}
