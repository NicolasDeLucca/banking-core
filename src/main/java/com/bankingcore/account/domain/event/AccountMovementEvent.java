package com.bankingcore.account.domain.event;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Published whenever an account's balance changes. Plain data, no framework
 * dependency: account publishes it through Spring's ApplicationEventPublisher
 * (an application-layer concern) without knowing who, if anyone, is listening.
 * The transaction module listens to build its audit trail (see
 * transaction.infrastructure.event.AccountMovementEventListener).
 */
public record AccountMovementEvent(
        Long accountId,
        AccountMovementType movementType,
        BigDecimal amount,
        Instant occurredAt,
        Long relatedAccountId
) {

    public static AccountMovementEvent of(Long accountId, AccountMovementType movementType, BigDecimal amount, Instant occurredAt) {
        return new AccountMovementEvent(accountId, movementType, amount, occurredAt, null);
    }
}
