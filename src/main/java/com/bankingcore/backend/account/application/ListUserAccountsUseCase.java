package com.bankingcore.backend.account.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bankingcore.backend.account.domain.AccountRepository;

@Service
public class ListUserAccountsUseCase {

    private final AccountRepository accountRepository;

    public ListUserAccountsUseCase(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional(readOnly = true)
    public List<AccountResult> execute(Long ownerId) {
        return accountRepository.findAllByOwnerId(ownerId).stream()
                .map(AccountResult::from)
                .toList();
    }
}
