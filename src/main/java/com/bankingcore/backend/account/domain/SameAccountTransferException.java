package com.bankingcore.backend.account.domain;

import com.bankingcore.backend.shared.error.BusinessRuleViolationException;

public class SameAccountTransferException extends BusinessRuleViolationException {
    public SameAccountTransferException(Long accountId) {
        super("Cannot transfer to the same account: " + accountId, "SAME_ACCOUNT_TRANSFER");
    }
}
