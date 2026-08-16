package com.bankingcore.transaction.domain;

import java.util.List;

public interface TransactionRepository {

    Transaction save(Transaction transaction);

    List<Transaction> findAllByAccountId(Long accountId);
}
