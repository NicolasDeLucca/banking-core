package com.bankingcore.backend.account.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Value Object representing a non-negative monetary amount. Immutable by design:
 * every operation returns a new instance instead of mutating state.
 */
public final class Money {

    private static final int SCALE = 2;

    private final BigDecimal amount;

    private Money(BigDecimal amount) {
        this.amount = amount;
    }

    public static Money of(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        return new Money(amount.setScale(SCALE, RoundingMode.HALF_UP));
    }

    public static Money zero() {
        return new Money(BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP));
    }

    public Money add(Money other) {
        return new Money(this.amount.add(other.amount));
    }

    /** @throws IllegalStateException if the result would be negative (callers must check sufficiency first) */
    public Money subtract(Money other) {
        BigDecimal result = this.amount.subtract(other.amount);
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("Resulting amount cannot be negative");
        }
        return new Money(result);
    }

    public boolean isLessThan(Money other) {
        return this.amount.compareTo(other.amount) < 0;
    }

    public boolean isZero() {
        return this.amount.compareTo(BigDecimal.ZERO) == 0;
    }

    public boolean isPositive() {
        return this.amount.compareTo(BigDecimal.ZERO) > 0;
    }

    public BigDecimal amount() {
        return amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money other)) return false;
        return this.amount.compareTo(other.amount) == 0;
    }

    @Override
    public int hashCode() {
        return amount.stripTrailingZeros().hashCode();
    }

    @Override
    public String toString() {
        return amount.toString();
    }
}
