package com.bankingcore.account.domain;

import com.bankingcore.shared.error.BusinessRuleViolationException;

public class InvalidAmountException extends BusinessRuleViolationException {
    public InvalidAmountException() {
        super("Amount must be greater than zero", "INVALID_AMOUNT");
    }
}
