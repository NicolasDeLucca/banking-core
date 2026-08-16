package com.bankingcore.account.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bankingcore.account.domain.Account;
import com.bankingcore.account.domain.AccountNotFoundException;
import com.bankingcore.account.domain.AccountRepository;

@Service
public class GetAccountDetailsUseCase {

    private final AccountRepository accountRepository;

    public GetAccountDetailsUseCase(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional(readOnly = true)
    public AccountResult execute(Long accountId, Long requesterId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
        account.verifyOwnedBy(requesterId);
        return AccountResult.from(account);
    }
}
