package com.bankingcore.account.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bankingcore.account.domain.AccountRepository;
import com.bankingcore.shared.paging.PageRequest;

/** No ownership check by design: reachable only through an ADMIN-protected route. */
@Service
public class AdminListAllAccountsUseCase {

    private final AccountRepository accountRepository;

    public AdminListAllAccountsUseCase(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional(readOnly = true)
    public List<AccountResult> execute(Integer page, Integer size) {
        return accountRepository.findAll(PageRequest.of(page, size)).stream().map(AccountResult::from).toList();
    }
}
