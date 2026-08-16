package com.bankingcore.account.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.bankingcore.account.domain.Account;
import com.bankingcore.account.domain.AccountRepository;

@Repository
public class JpaAccountRepositoryAdapter implements AccountRepository {

    private final AccountJpaRepository jpaRepository;
    private final AccountMapper mapper;

    public JpaAccountRepositoryAdapter(AccountJpaRepository jpaRepository, AccountMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Account save(Account account) {
        AccountJpaEntity entity;

        if (account.getId() == null) {
            entity = mapper.toNewJpaEntity(account);
        } else {
            // Loaded within the same transaction as the original findById, so this
            // resolves from Hibernate's persistence context (no extra round trip)
            // and lets @Version detect concurrent modifications on flush.
            entity = jpaRepository.findById(account.getId())
                    .orElseThrow(() -> new IllegalStateException("Account " + account.getId() + " was expected to exist"));
            mapper.updateJpaEntity(entity, account);
        }

        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Account> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Account> findAllByOwnerId(Long ownerId) {
        return jpaRepository.findAllByOwnerId(ownerId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Account> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
    }
}
