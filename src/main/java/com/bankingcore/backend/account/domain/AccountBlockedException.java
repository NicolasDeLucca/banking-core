package com.bankingcore.backend.account.domain;

import com.bankingcore.backend.shared.error.BusinessRuleViolationException;

public class AccountBlockedException extends BusinessRuleViolationException {
    public AccountBlockedException(Long accountId) {
        super("Account " + accountId + " is blocked and cannot operate", "ACCOUNT_BLOCKED");
    }
}
