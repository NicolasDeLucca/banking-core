package com.bankingcore.backend.auth.infrastructure.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.bankingcore.backend.auth.domain.User;
import com.bankingcore.backend.auth.domain.UserRepository;

/**
 * Adapts the domain-facing UserRepository port to Spring Data JPA. This is the
 * only class allowed to know both the domain model and the JPA model.
 */
@Repository
public class JpaUserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository jpaRepository;
    private final UserMapper mapper;

    public JpaUserRepositoryAdapter(UserJpaRepository jpaRepository, UserMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public User save(User user) {
        UserJpaEntity saved = jpaRepository.save(mapper.toJpaEntity(user));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }
}
