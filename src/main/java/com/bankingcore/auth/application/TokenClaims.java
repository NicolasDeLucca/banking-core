package com.bankingcore.auth.application;

import com.bankingcore.auth.domain.UserRole;

/** Identity information extracted from a validated authentication token. */
public record TokenClaims(Long userId, String email, UserRole role) {
}
