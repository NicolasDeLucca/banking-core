package com.bankingcore.backend.account.infrastructure.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

interface AccountJpaRepository extends JpaRepository<AccountJpaEntity, Long> {
    List<AccountJpaEntity> findAllByOwnerId(Long ownerId);
}
