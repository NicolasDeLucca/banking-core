package com.bankingcore.auth.infrastructure.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.Test;

import com.bankingcore.auth.application.TokenClaims;
import com.bankingcore.auth.domain.UserRole;
import com.bankingcore.shared.error.AuthenticationFailedException;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenProviderTest {

    private static final String SECRET = "unit-test-only-secret-key-at-least-32-chars-long";

    private final JwtTokenProvider provider = new JwtTokenProvider(SECRET, 3600);

    @Test
    void generatedTokenRoundTripsToTheSameClaims() {
        String token = provider.generateToken(42L, "user@example.com", UserRole.USER);

        TokenClaims claims = provider.parseToken(token);

        assertThat(claims.userId()).isEqualTo(42L);
        assertThat(claims.email()).isEqualTo("user@example.com");
        assertThat(claims.role()).isEqualTo(UserRole.USER);
    }

    @Test
    void rejectsAGarbageToken() {
        assertThatThrownBy(() -> provider.parseToken("not-a-jwt-at-all"))
                .isInstanceOf(AuthenticationFailedException.class);
    }

    @Test
    void rejectsATokenSignedWithADifferentSecret() {
        String token = new JwtTokenProvider("a-completely-different-secret-of-at-least-32-chars", 3600)
                .generateToken(1L, "user@example.com", UserRole.USER);

        assertThatThrownBy(() -> provider.parseToken(token))
                .isInstanceOf(AuthenticationFailedException.class);
    }

    @Test
    void rejectsAnExpiredToken() {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        Instant past = Instant.now().minusSeconds(3600);
        String expiredToken = Jwts.builder()
                .subject("1")
                .claim("email", "user@example.com")
                .claim("role", UserRole.USER.name())
                .issuedAt(Date.from(past.minusSeconds(60)))
                .expiration(Date.from(past))
                .signWith(key)
                .compact();

        assertThatThrownBy(() -> provider.parseToken(expiredToken))
                .isInstanceOf(AuthenticationFailedException.class);
    }

    /**
     * Regression test: a token with a valid signature but a missing "role" claim
     * used to leak a raw NullPointerException out of parseToken (Enum.valueOf(null)
     * throws NPE, not IllegalArgumentException), instead of a clean auth failure.
     */
    @Test
    void rejectsATokenMissingTheRoleClaimInsteadOfThrowingNpe() {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        Instant now = Instant.now();
        String tokenWithoutRole = Jwts.builder()
                .subject("1")
                .claim("email", "user@example.com")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(3600)))
                .signWith(key)
                .compact();

        assertThatThrownBy(() -> provider.parseToken(tokenWithoutRole))
                .isInstanceOf(AuthenticationFailedException.class);
    }
}
