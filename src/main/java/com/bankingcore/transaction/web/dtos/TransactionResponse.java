package com.bankingcore.transaction.web.dtos;

import java.math.BigDecimal;
import java.time.Instant;

import com.bankingcore.transaction.domain.TransactionType;

public record TransactionResponse(
        Long id,
        Long accountId,
        Long relatedAccountId,
        TransactionType type,
        BigDecimal amount,
        Instant occurredAt
) {
}
