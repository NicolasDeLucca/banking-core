package com.bankingcore.backend.account.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bankingcore.backend.account.domain.AccountRepository;

/** No ownership check by design: reachable only through an ADMIN-protected route. */
@Service
public class AdminListAllAccountsUseCase {

    private final AccountRepository accountRepository;

    public AdminListAllAccountsUseCase(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional(readOnly = true)
    public List<AccountResult> execute() {
        return accountRepository.findAll().stream().map(AccountResult::from).toList();
    }
}
