package com.bankingcore.account.application;

import java.time.Instant;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bankingcore.account.domain.Account;
import com.bankingcore.account.domain.AccountNotFoundException;
import com.bankingcore.account.domain.AccountRepository;
import com.bankingcore.account.domain.event.AccountLifecycleAction;
import com.bankingcore.account.domain.event.AccountLifecycleEvent;

@Service
public class CloseAccountUseCase {

    private final AccountRepository accountRepository;
    private final ApplicationEventPublisher eventPublisher;

    public CloseAccountUseCase(AccountRepository accountRepository, ApplicationEventPublisher eventPublisher) {
        this.accountRepository = accountRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public AccountResult execute(Long accountId, Long requesterId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
        account.verifyOwnedBy(requesterId);
        account.close();
        Account saved = accountRepository.save(account);

        eventPublisher.publishEvent(new AccountLifecycleEvent(saved.getId(), requesterId, AccountLifecycleAction.CLOSED, Instant.now()));

        return AccountResult.from(saved);
    }
}
