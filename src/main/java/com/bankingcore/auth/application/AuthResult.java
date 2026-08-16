package com.bankingcore.auth.application;

/** Outcome of a successful registration or login, before it is shaped into a web response. */
public record AuthResult(Long userId, String email, String token) {
}
