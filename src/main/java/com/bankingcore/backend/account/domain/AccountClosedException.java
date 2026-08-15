package com.bankingcore.backend.account.domain;

import com.bankingcore.backend.shared.error.BusinessRuleViolationException;

public class AccountClosedException extends BusinessRuleViolationException {
    public AccountClosedException(Long accountId) {
        super("Account " + accountId + " is closed and cannot operate", "ACCOUNT_CLOSED");
    }
}
