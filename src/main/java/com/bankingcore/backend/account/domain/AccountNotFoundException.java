package com.bankingcore.backend.account.domain;

import com.bankingcore.backend.shared.error.NotFoundException;

public class AccountNotFoundException extends NotFoundException {
    public AccountNotFoundException(Long accountId) {
        super("Account not found: " + accountId, "ACCOUNT_NOT_FOUND");
    }
}
