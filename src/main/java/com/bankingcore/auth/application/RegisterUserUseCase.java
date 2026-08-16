package com.bankingcore.auth.application;

import java.time.Instant;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bankingcore.auth.domain.EmailAlreadyRegisteredException;
import com.bankingcore.auth.domain.User;
import com.bankingcore.auth.domain.UserRepository;
import com.bankingcore.auth.domain.event.UserRegisteredEvent;

@Service
public class RegisterUserUseCase {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final TokenProvider tokenProvider;
    private final ApplicationEventPublisher eventPublisher;

    public RegisterUserUseCase(
            UserRepository userRepository,
            PasswordHasher passwordHasher,
            TokenProvider tokenProvider,
            ApplicationEventPublisher eventPublisher
    ) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.tokenProvider = tokenProvider;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public AuthResult execute(String email, String rawPassword) {
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyRegisteredException(email);
        }

        User user = User.register(email, passwordHasher.hash(rawPassword));
        User saved = userRepository.save(user);

        String token = tokenProvider.generateToken(saved.getId(), saved.getEmail(), saved.getRole());

        eventPublisher.publishEvent(new UserRegisteredEvent(saved.getId(), saved.getEmail(), Instant.now()));

        return new AuthResult(saved.getId(), saved.getEmail(), token);
    }
}
