package com.bankingcore.transaction.domain;

import java.util.List;

import com.bankingcore.shared.paging.PageRequest;

public interface TransactionRepository {

    Transaction save(Transaction transaction);

    List<Transaction> findAllByAccountId(Long accountId, PageRequest pageRequest);
}
