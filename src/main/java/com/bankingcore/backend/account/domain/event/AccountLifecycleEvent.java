package com.bankingcore.backend.account.domain.event;

import java.time.Instant;

/** @param actorUserId whoever performed the action: the owner for self-service, an admin for administrative actions */
public record AccountLifecycleEvent(Long accountId, Long actorUserId, AccountLifecycleAction action, Instant occurredAt) {
}
