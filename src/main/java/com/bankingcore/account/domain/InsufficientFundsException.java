package com.bankingcore.account.domain;

import com.bankingcore.shared.error.BusinessRuleViolationException;

public class InsufficientFundsException extends BusinessRuleViolationException {
    public InsufficientFundsException(Long accountId) {
        super("Account " + accountId + " has insufficient funds for this operation", "INSUFFICIENT_FUNDS");
    }
}
