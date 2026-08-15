package com.bankingcore.backend.auth.application;

/**
 * Abstraction over the password hashing mechanism. Implemented in
 * auth.infrastructure.security using Spring Security's BCrypt, so the
 * application layer never depends on Spring Security directly.
 */
public interface PasswordHasher {

    String hash(String rawPassword);

    boolean matches(String rawPassword, String hashedPassword);
}
