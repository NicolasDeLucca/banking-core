package com.bankingcore.backend.account.application;

import java.time.Instant;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bankingcore.backend.account.domain.Account;
import com.bankingcore.backend.account.domain.AccountRepository;
import com.bankingcore.backend.account.domain.AccountType;
import com.bankingcore.backend.account.domain.event.AccountLifecycleAction;
import com.bankingcore.backend.account.domain.event.AccountLifecycleEvent;

@Service
public class CreateAccountUseCase {

    private final AccountRepository accountRepository;
    private final ApplicationEventPublisher eventPublisher;

    public CreateAccountUseCase(AccountRepository accountRepository, ApplicationEventPublisher eventPublisher) {
        this.accountRepository = accountRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public AccountResult execute(Long ownerId, AccountType type) {
        Account account = Account.open(ownerId, type);
        Account saved = accountRepository.save(account);

        eventPublisher.publishEvent(new AccountLifecycleEvent(saved.getId(), ownerId, AccountLifecycleAction.OPENED, Instant.now()));

        return AccountResult.from(saved);
    }
}
