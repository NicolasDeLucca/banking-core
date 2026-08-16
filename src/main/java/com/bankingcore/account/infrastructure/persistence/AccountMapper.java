package com.bankingcore.account.infrastructure.persistence;

import org.springframework.stereotype.Component;

import com.bankingcore.account.domain.Account;
import com.bankingcore.account.domain.Money;

@Component
public class AccountMapper {

    public AccountJpaEntity toNewJpaEntity(Account account) {
        return new AccountJpaEntity(null, account.getOwnerId(), account.getType(), account.getBalance().amount(), account.getStatus());
    }

    /**
     * Applies domain state onto an already-managed JPA entity instance, instead of
     * building a new one, so Hibernate's dirty checking and @Version-based
     * optimistic locking work correctly. ownerId/type never change after creation.
     */
    public void updateJpaEntity(AccountJpaEntity entity, Account account) {
        entity.setBalance(account.getBalance().amount());
        entity.setStatus(account.getStatus());
    }

    public Account toDomain(AccountJpaEntity entity) {
        return Account.reconstitute(
                entity.getId(),
                entity.getOwnerId(),
                entity.getType(),
                Money.of(entity.getBalance()),
                entity.getStatus()
        );
    }
}
