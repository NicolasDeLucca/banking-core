package com.bankingcore.backend.account.domain;

import com.bankingcore.backend.shared.error.BusinessRuleViolationException;

public class InsufficientFundsException extends BusinessRuleViolationException {
    public InsufficientFundsException(Long accountId) {
        super("Account " + accountId + " has insufficient funds for this operation", "INSUFFICIENT_FUNDS");
    }
}
