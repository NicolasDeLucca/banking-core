package com.bankingcore.backend.account.application;

import java.math.BigDecimal;

import com.bankingcore.backend.account.domain.Account;
import com.bankingcore.backend.account.domain.AccountStatus;
import com.bankingcore.backend.account.domain.AccountType;

/** Plain projection of an Account, ready to be shaped into a web response. */
public record AccountResult(Long id, Long ownerId, AccountType type, BigDecimal balance, AccountStatus status) {

    public static AccountResult from(Account account) {
        return new AccountResult(
                account.getId(),
                account.getOwnerId(),
                account.getType(),
                account.getBalance().amount(),
                account.getStatus()
        );
    }
}
