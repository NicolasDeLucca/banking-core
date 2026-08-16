package com.bankingcore.auth.domain;

import java.util.Optional;

/**
 * Abstraction the application layer depends on to persist/retrieve users.
 * Implemented by auth.infrastructure.persistence.JpaUserRepositoryAdapter,
 * keeping the domain/application layers unaware of JPA (Opción B / DIP).
 */
public interface UserRepository {

    User save(User user);

    Optional<User> findByEmail(String email);

    Optional<User> findById(Long id);

    boolean existsByEmail(String email);
}
