package com.bankingcore.backend.auth;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bankingcore.backend.auth.dtos.AuthResponse;
import com.bankingcore.backend.auth.dtos.LoginRequest;
import com.bankingcore.backend.auth.dtos.RegisterRequest;
import com.bankingcore.backend.shared.error.DomainException;
import com.bankingcore.backend.shared.security.JwtService;
import com.bankingcore.backend.users.UserEntity;
import com.bankingcore.backend.users.UserRepository;
import com.bankingcore.backend.users.UserRole;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new DomainException("Email already registered", HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS");
        }

        UserEntity user = new UserEntity();
        user.setEmail(request.email().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(UserRole.USER);

        UserEntity saved = userRepository.save(user);
        String token = jwtService.createToken(saved);
        return new AuthResponse(saved.getId(), saved.getEmail(), token);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        UserEntity user = userRepository.findByEmail(request.email().toLowerCase())
                .orElseThrow(() -> new DomainException("Invalid credentials", HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new DomainException("Invalid credentials", HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS");
        }

        String token = jwtService.createToken(user);
        return new AuthResponse(user.getId(), user.getEmail(), token);
    }
}