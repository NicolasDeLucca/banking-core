package com.bankingcore.backend.auth.domain;

import com.bankingcore.backend.shared.error.AuthenticationFailedException;

public class InvalidCredentialsException extends AuthenticationFailedException {
    public InvalidCredentialsException() {
        super("Invalid credentials", "INVALID_CREDENTIALS");
    }
}
