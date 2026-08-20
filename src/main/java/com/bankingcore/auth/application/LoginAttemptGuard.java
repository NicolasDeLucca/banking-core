package com.bankingcore.auth.application;

/**
 * Abstraction over brute-force protection for login attempts. Implemented in
 * auth.infrastructure.security, so the application layer never depends on how
 * attempts are actually tracked (in-memory here; a distributed store like
 * Redis would be a drop-in replacement behind this same interface if this
 * service ever ran as more than one instance).
 */
public interface LoginAttemptGuard {

    /**
     * @throws com.bankingcore.auth.domain.TooManyLoginAttemptsException if this email is currently locked out
     */
    void checkAllowed(String normalizedEmail);

    void recordFailure(String normalizedEmail);

    void recordSuccess(String normalizedEmail);
}
