package com.bankingcore.backend.account.domain;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

    @Test
    void ofRejectsNullAmount() {
        assertThatThrownBy(() -> Money.of(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void ofRejectsNegativeAmount() {
        assertThatThrownBy(() -> Money.of(new BigDecimal("-1"))).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void ofRoundsToTwoDecimals() {
        assertThat(Money.of(new BigDecimal("10.005")).amount()).isEqualByComparingTo("10.01");
    }

    @Test
    void addSumsAmounts() {
        Money result = Money.of(new BigDecimal("10")).add(Money.of(new BigDecimal("5")));
        assertThat(result.amount()).isEqualByComparingTo("15.00");
    }

    @Test
    void subtractReducesAmount() {
        Money result = Money.of(new BigDecimal("10")).subtract(Money.of(new BigDecimal("4")));
        assertThat(result.amount()).isEqualByComparingTo("6.00");
    }

    @Test
    void subtractThrowsWhenResultWouldBeNegative() {
        Money ten = Money.of(BigDecimal.TEN);
        Money twenty = Money.of(new BigDecimal("20"));
        assertThatThrownBy(() -> ten.subtract(twenty)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void zeroIsZeroAndNotPositive() {
        assertThat(Money.zero().isZero()).isTrue();
        assertThat(Money.zero().isPositive()).isFalse();
    }

    @Test
    void equalityIgnoresScaleDifferences() {
        assertThat(Money.of(new BigDecimal("10"))).isEqualTo(Money.of(new BigDecimal("10.00")));
    }
}
