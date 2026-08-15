package com.bankingcore.backend.account.web.dtos;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record TransferRequest(
    @NotNull Long destinationAccountId,
    @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal amount
) {
}
