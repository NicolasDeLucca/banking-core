package com.bankingcore.account.domain;

import com.bankingcore.shared.error.NotFoundException;

public class AccountNotFoundException extends NotFoundException {
    public AccountNotFoundException(Long accountId) {
        super("Account not found: " + accountId, "ACCOUNT_NOT_FOUND");
    }
}
