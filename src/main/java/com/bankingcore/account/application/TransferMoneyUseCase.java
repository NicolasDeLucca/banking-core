package com.bankingcore.account.application;

import java.math.BigDecimal;
import java.time.Instant;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bankingcore.account.domain.Account;
import com.bankingcore.account.domain.AccountNotFoundException;
import com.bankingcore.account.domain.AccountRepository;
import com.bankingcore.account.domain.Money;
import com.bankingcore.account.domain.SameAccountTransferException;
import com.bankingcore.account.domain.event.AccountMovementEvent;
import com.bankingcore.account.domain.event.AccountMovementType;

/**
 * Orchestrates a transfer as two Account operations (withdraw + deposit), reusing
 * every rule already encapsulated in Account (funds, status) instead of duplicating
 * them. Only the source account's ownership is checked: the destination can belong
 * to anyone, same as a real bank transfer.
 */
@Service
public class TransferMoneyUseCase {

    private final AccountRepository accountRepository;
    private final ApplicationEventPublisher eventPublisher;

    public TransferMoneyUseCase(AccountRepository accountRepository, ApplicationEventPublisher eventPublisher) {
        this.accountRepository = accountRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public TransferResult execute(Long sourceAccountId, Long destinationAccountId, Long requesterId, BigDecimal amount) {
        if (sourceAccountId.equals(destinationAccountId)) {
            throw new SameAccountTransferException(sourceAccountId);
        }

        Account source = accountRepository.findById(sourceAccountId)
                .orElseThrow(() -> new AccountNotFoundException(sourceAccountId));
        source.verifyOwnedBy(requesterId);

        Account destination = accountRepository.findById(destinationAccountId)
                .orElseThrow(() -> new AccountNotFoundException(destinationAccountId));

        Money transferAmount = Money.of(amount);
        source.withdraw(transferAmount);
        destination.deposit(transferAmount);

        Account savedSource = accountRepository.save(source);
        Account savedDestination = accountRepository.save(destination);

        Instant now = Instant.now();
        eventPublisher.publishEvent(new AccountMovementEvent(
                savedSource.getId(), AccountMovementType.TRANSFER_OUT, amount, now, savedDestination.getId()));
        eventPublisher.publishEvent(new AccountMovementEvent(
                savedDestination.getId(), AccountMovementType.TRANSFER_IN, amount, now, savedSource.getId()));

        return new TransferResult(AccountResult.from(savedSource), AccountResult.from(savedDestination));
    }
}
