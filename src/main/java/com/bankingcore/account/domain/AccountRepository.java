package com.bankingcore.account.domain;

import java.util.List;
import java.util.Optional;

import com.bankingcore.shared.paging.PageRequest;

public interface AccountRepository {

    Account save(Account account);

    Optional<Account> findById(Long id);

    List<Account> findAllByOwnerId(Long ownerId);

    /** Administrative use only — never exposed to a non-admin use case. */
    List<Account> findAll(PageRequest pageRequest);
}
