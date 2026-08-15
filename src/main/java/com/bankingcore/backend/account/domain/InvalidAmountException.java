package com.bankingcore.backend.account.domain;

import com.bankingcore.backend.shared.error.BusinessRuleViolationException;

public class InvalidAmountException extends BusinessRuleViolationException {
    public InvalidAmountException() {
        super("Amount must be greater than zero", "INVALID_AMOUNT");
    }
}
