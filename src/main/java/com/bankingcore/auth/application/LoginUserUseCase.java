package com.bankingcore.auth.application;

import java.time.Instant;
import java.util.Locale;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bankingcore.auth.domain.InvalidCredentialsException;
import com.bankingcore.auth.domain.User;
import com.bankingcore.auth.domain.UserRepository;
import com.bankingcore.auth.domain.event.UserAuthenticationEvent;

@Service
public class LoginUserUseCase {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final TokenProvider tokenProvider;
    private final LoginAttemptGuard loginAttemptGuard;
    private final ApplicationEventPublisher eventPublisher;

    public LoginUserUseCase(
            UserRepository userRepository,
            PasswordHasher passwordHasher,
            TokenProvider tokenProvider,
            LoginAttemptGuard loginAttemptGuard,
            ApplicationEventPublisher eventPublisher
    ) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.tokenProvider = tokenProvider;
        this.loginAttemptGuard = loginAttemptGuard;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public AuthResult execute(String email, String rawPassword) {
        String normalizedEmail = normalize(email);

        // Checked before touching the DB or hashing anything, both to fail
        // fast under an ongoing brute-force attempt and so a locked-out email
        // never reaches the password-hash comparison at all.
        loginAttemptGuard.checkAllowed(normalizedEmail);

        Optional<User> maybeUser = userRepository.findByEmail(normalizedEmail);
        boolean credentialsValid = maybeUser.isPresent() && passwordHasher.matches(rawPassword, maybeUser.get().getPasswordHash());

        // Published even on failure: the listener runs AFTER_COMPLETION so the audit
        // entry survives the rollback triggered by the exception thrown below.
        eventPublisher.publishEvent(new UserAuthenticationEvent(
                maybeUser.map(User::getId).orElse(null), normalizedEmail, credentialsValid, Instant.now()));

        if (!credentialsValid) {
            loginAttemptGuard.recordFailure(normalizedEmail);
            throw new InvalidCredentialsException();
        }
        loginAttemptGuard.recordSuccess(normalizedEmail);

        User user = maybeUser.get();
        String token = tokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole());
        return new AuthResult(user.getId(), user.getEmail(), token);
    }

    private static String normalize(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
