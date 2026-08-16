package com.bankingcore.auth.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data technical repository. Never exposed outside the infrastructure layer. */
interface UserJpaRepository extends JpaRepository<UserJpaEntity, Long> {
    Optional<UserJpaEntity> findByEmail(String email);

    boolean existsByEmail(String email);
}
