package com.bankingcore.backend.account.application;

import java.time.Instant;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bankingcore.backend.account.domain.Account;
import com.bankingcore.backend.account.domain.AccountNotFoundException;
import com.bankingcore.backend.account.domain.AccountRepository;
import com.bankingcore.backend.account.domain.event.AccountLifecycleAction;
import com.bankingcore.backend.account.domain.event.AccountLifecycleEvent;

@Service
public class AdminBlockAccountUseCase {

    private final AccountRepository accountRepository;
    private final ApplicationEventPublisher eventPublisher;

    public AdminBlockAccountUseCase(AccountRepository accountRepository, ApplicationEventPublisher eventPublisher) {
        this.accountRepository = accountRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public AccountResult execute(Long accountId, Long adminUserId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
        account.block();
        Account saved = accountRepository.save(account);

        eventPublisher.publishEvent(new AccountLifecycleEvent(saved.getId(), adminUserId, AccountLifecycleAction.BLOCKED, Instant.now()));

        return AccountResult.from(saved);
    }
}
