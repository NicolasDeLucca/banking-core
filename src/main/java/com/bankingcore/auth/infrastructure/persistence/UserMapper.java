package com.bankingcore.auth.infrastructure.persistence;

import org.springframework.stereotype.Component;

import com.bankingcore.auth.domain.User;

/** Pure, dependency-free conversion between the domain model and the JPA model. */
@Component
public class UserMapper {

    public UserJpaEntity toJpaEntity(User user) {
        return new UserJpaEntity(user.getId(), user.getEmail(), user.getPasswordHash(), user.getRole());
    }

    public User toDomain(UserJpaEntity entity) {
        return User.reconstitute(entity.getId(), entity.getEmail(), entity.getPasswordHash(), entity.getRole());
    }
}
