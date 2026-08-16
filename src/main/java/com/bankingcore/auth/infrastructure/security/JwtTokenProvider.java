package com.bankingcore.auth.infrastructure.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.bankingcore.auth.application.TokenClaims;
import com.bankingcore.auth.application.TokenProvider;
import com.bankingcore.auth.domain.UserRole;
import com.bankingcore.shared.error.AuthenticationFailedException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/** JWT implementation of TokenProvider. Nothing outside this package knows about JJWT. */
@Component
public class JwtTokenProvider implements TokenProvider {

    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_ROLE = "role";

    private final SecretKey key;
    private final long expirationSeconds;

    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-seconds}") long expirationSeconds
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationSeconds = expirationSeconds;
    }

    @Override
    public String generateToken(Long userId, String email, UserRole role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId.toString())
                .claim(CLAIM_EMAIL, email)
                .claim(CLAIM_ROLE, role.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expirationSeconds)))
                .signWith(key)
                .compact();
    }

    @Override
    public TokenClaims parseToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Long userId = Long.valueOf(claims.getSubject());
            String email = claims.get(CLAIM_EMAIL, String.class);
            UserRole role = UserRole.valueOf(claims.get(CLAIM_ROLE, String.class));
            return new TokenClaims(userId, email, role);
        } catch (RuntimeException exception) { // NOPMD - AvoidCatchingGenericException
            // Broad on purpose: a token is client-supplied, untrusted input. Any
            // failure while parsing or reading its claims - including a missing
            // "role" claim, which makes Enum.valueOf throw a NullPointerException
            // rather than an IllegalArgumentException - must become "invalid
            // token", never an unhandled exception out of the security filter.
            throw new InvalidTokenException(exception);
        }
    }

    private static class InvalidTokenException extends AuthenticationFailedException {
        InvalidTokenException(Throwable cause) {
            super("Invalid or expired token", "INVALID_TOKEN", cause);
        }
    }
}
