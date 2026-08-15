package com.bankingcore.backend.transaction.web.dtos;

import java.math.BigDecimal;
import java.time.Instant;

import com.bankingcore.backend.transaction.domain.TransactionType;

public record TransactionResponse(
        Long id,
        Long accountId,
        Long relatedAccountId,
        TransactionType type,
        BigDecimal amount,
        Instant occurredAt
) {
}
