package com.bankingcore.backend.account.domain;

import com.bankingcore.backend.shared.error.ForbiddenOperationException;

public class UnauthorizedAccountAccessException extends ForbiddenOperationException {
    public UnauthorizedAccountAccessException(Long accountId) {
        super("You are not allowed to access account " + accountId, "ACCOUNT_ACCESS_DENIED");
    }
}
