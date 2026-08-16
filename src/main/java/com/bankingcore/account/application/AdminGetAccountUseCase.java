package com.bankingcore.account.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bankingcore.account.domain.Account;
import com.bankingcore.account.domain.AccountNotFoundException;
import com.bankingcore.account.domain.AccountRepository;

/** No ownership check by design: reachable only through an ADMIN-protected route. */
@Service
public class AdminGetAccountUseCase {

    private final AccountRepository accountRepository;

    public AdminGetAccountUseCase(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional(readOnly = true)
    public AccountResult execute(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
        return AccountResult.from(account);
    }
}
