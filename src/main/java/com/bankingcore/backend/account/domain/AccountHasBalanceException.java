package com.bankingcore.backend.account.domain;

import com.bankingcore.backend.shared.error.BusinessRuleViolationException;

public class AccountHasBalanceException extends BusinessRuleViolationException {
    public AccountHasBalanceException(Long accountId) {
        super("Account " + accountId + " cannot be closed while it has a non-zero balance", "ACCOUNT_HAS_BALANCE");
    }
}
