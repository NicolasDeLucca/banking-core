package com.bankingcore.transaction.domain;

import java.math.BigDecimal;
import java.time.Instant;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransactionTest {

    @Test
    void recordRejectsNonPositiveAmount() {
        assertThatThrownBy(() -> Transaction.record(1L, null, TransactionType.DEPOSIT, BigDecimal.ZERO, Instant.now()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void recordCreatesAnUnsavedTransactionWithTheGivenData() {
        Instant now = Instant.now();

        Transaction transaction = Transaction.record(1L, 2L, TransactionType.TRANSFER_OUT, BigDecimal.TEN, now);

        assertThat(transaction.getId()).isNull();
        assertThat(transaction.getAccountId()).isEqualTo(1L);
        assertThat(transaction.getRelatedAccountId()).isEqualTo(2L);
        assertThat(transaction.getType()).isEqualTo(TransactionType.TRANSFER_OUT);
        assertThat(transaction.getAmount()).isEqualByComparingTo(BigDecimal.TEN);
        assertThat(transaction.getOccurredAt()).isEqualTo(now);
    }
}
