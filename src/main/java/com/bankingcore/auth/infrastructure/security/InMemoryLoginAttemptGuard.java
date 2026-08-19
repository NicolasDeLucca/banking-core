package com.bankingcore.auth.infrastructure.security;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.bankingcore.auth.application.LoginAttemptGuard;
import com.bankingcore.auth.domain.TooManyLoginAttemptsException;

/**
 * Per-instance, in-memory failed-login tracking: a fixed window of failures
 * per (normalized) email trips a lockout for a configurable duration. This
 * resets on restart and doesn't coordinate across instances - fine for this
 * service's current single-deployable shape (see docs/4+1-views.md's
 * Physical View); a multi-instance deployment would need this backed by a
 * shared store (e.g. Redis) instead, behind the same LoginAttemptGuard
 * interface.
 */
@Component
public class InMemoryLoginAttemptGuard implements LoginAttemptGuard {

    private final int maxAttempts;
    private final Duration window;
    private final Duration lockoutDuration;

    private final Map<String, Attempts> attemptsByEmail = new ConcurrentHashMap<>();

    public InMemoryLoginAttemptGuard(
            @Value("${app.security.login.max-attempts:5}") int maxAttempts,
            @Value("${app.security.login.window-minutes:15}") long windowMinutes,
            @Value("${app.security.login.lockout-minutes:15}") long lockoutMinutes
    ) {
        this.maxAttempts = maxAttempts;
        this.window = Duration.ofMinutes(windowMinutes);
        this.lockoutDuration = Duration.ofMinutes(lockoutMinutes);
    }

    @Override
    public void checkAllowed(String normalizedEmail) {
        Attempts attempts = attemptsByEmail.get(normalizedEmail);
        if (attempts != null && attempts.isLockedOut(Instant.now())) {
            throw new TooManyLoginAttemptsException();
        }
    }

    @Override
    public void recordFailure(String normalizedEmail) {
        Instant now = Instant.now();
        attemptsByEmail.compute(normalizedEmail, (email, existing) -> {
            Attempts attempts = existing != null ? existing : new Attempts();
            attempts.registerFailure(now, window, lockoutDuration, maxAttempts);
            return attempts;
        });
    }

    @Override
    public void recordSuccess(String normalizedEmail) {
        attemptsByEmail.remove(normalizedEmail);
    }

    /** Mutable only via the ConcurrentHashMap's atomic compute() above - never touched outside it. */
    private static final class Attempts {
        private int failureCount;
        private Instant windowStart;
        private Instant lockedUntil;

        boolean isLockedOut(Instant now) {
            return lockedUntil != null && now.isBefore(lockedUntil);
        }

        void registerFailure(Instant now, Duration window, Duration lockoutDuration, int maxAttempts) {
            if (windowStart == null || Duration.between(windowStart, now).compareTo(window) > 0) {
                windowStart = now;
                failureCount = 0;
            }
            failureCount++;
            if (failureCount >= maxAttempts) {
                lockedUntil = now.plus(lockoutDuration);
            }
        }
    }
}
