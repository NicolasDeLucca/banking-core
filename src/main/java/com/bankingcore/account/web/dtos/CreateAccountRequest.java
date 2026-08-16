package com.bankingcore.account.web.dtos;

import com.bankingcore.account.domain.AccountType;

import jakarta.validation.constraints.NotNull;

public record CreateAccountRequest(
    @NotNull AccountType type
) {
}
