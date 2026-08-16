package com.bankingcore.account.domain;

import com.bankingcore.shared.error.BusinessRuleViolationException;

public class AccountBlockedException extends BusinessRuleViolationException {
    public AccountBlockedException(Long accountId) {
        super("Account " + accountId + " is blocked and cannot operate", "ACCOUNT_BLOCKED");
    }
}
