package com.bankingcore.backend.account.web.dtos;

import com.bankingcore.backend.account.domain.AccountType;

import jakarta.validation.constraints.NotNull;

public record CreateAccountRequest(
    @NotNull AccountType type
) {
}
