package com.bankingcore.backend.auth.application;

import com.bankingcore.backend.auth.domain.UserRole;

/**
 * Abstraction over the authentication token mechanism. Implemented in
 * auth.infrastructure.security via JWT (JwtTokenProvider). Swapping JWT for
 * another mechanism only requires a new implementation of this interface.
 */
public interface TokenProvider {

    String generateToken(Long userId, String email, UserRole role);

    /** @throws com.bankingcore.backend.shared.error.AuthenticationFailedException if the token is missing/invalid/expired */
    TokenClaims parseToken(String token);
}
