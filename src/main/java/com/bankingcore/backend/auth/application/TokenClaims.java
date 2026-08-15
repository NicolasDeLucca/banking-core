package com.bankingcore.backend.auth.application;

import com.bankingcore.backend.auth.domain.UserRole;

/** Identity information extracted from a validated authentication token. */
public record TokenClaims(Long userId, String email, UserRole role) {
}
