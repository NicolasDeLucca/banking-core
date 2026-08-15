package com.bankingcore.backend.transaction.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bankingcore.backend.account.domain.Account;
import com.bankingcore.backend.account.domain.AccountNotFoundException;
import com.bankingcore.backend.account.domain.AccountRepository;
import com.bankingcore.backend.transaction.domain.TransactionRepository;

/**
 * Depends on account.domain (one-directional: transaction -> account) purely to
 * verify that the requester owns the account before exposing its history. account
 * has no knowledge of transaction at all, so this dependency never cycles back.
 */
@Service
public class ListAccountTransactionsUseCase {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    public ListAccountTransactionsUseCase(TransactionRepository transactionRepository, AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional(readOnly = true)
    public List<TransactionResult> execute(Long accountId, Long requesterId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
        account.verifyOwnedBy(requesterId);

        return transactionRepository.findAllByAccountId(accountId).stream()
                .map(TransactionResult::from)
                .toList();
    }
}
