package com.bankingcore.account.domain;

import com.bankingcore.shared.error.ForbiddenOperationException;

public class UnauthorizedAccountAccessException extends ForbiddenOperationException {
    public UnauthorizedAccountAccessException(Long accountId) {
        super("You are not allowed to access account " + accountId, "ACCOUNT_ACCESS_DENIED");
    }
}
