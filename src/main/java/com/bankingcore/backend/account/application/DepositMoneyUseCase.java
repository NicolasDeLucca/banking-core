package com.bankingcore.backend.account.application;

import java.math.BigDecimal;
import java.time.Instant;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bankingcore.backend.account.domain.Account;
import com.bankingcore.backend.account.domain.AccountNotFoundException;
import com.bankingcore.backend.account.domain.AccountRepository;
import com.bankingcore.backend.account.domain.Money;
import com.bankingcore.backend.account.domain.event.AccountMovementEvent;
import com.bankingcore.backend.account.domain.event.AccountMovementType;

@Service
public class DepositMoneyUseCase {

    private final AccountRepository accountRepository;
    private final ApplicationEventPublisher eventPublisher;

    public DepositMoneyUseCase(AccountRepository accountRepository, ApplicationEventPublisher eventPublisher) {
        this.accountRepository = accountRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public AccountResult execute(Long accountId, Long requesterId, BigDecimal amount) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
        account.verifyOwnedBy(requesterId);
        account.deposit(Money.of(amount));
        Account saved = accountRepository.save(account);

        eventPublisher.publishEvent(AccountMovementEvent.of(saved.getId(), AccountMovementType.DEPOSIT, amount, Instant.now()));

        return AccountResult.from(saved);
    }
}
