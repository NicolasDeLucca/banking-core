package com.bankingcore.auth.domain;

import com.bankingcore.shared.error.AuthenticationFailedException;

public class InvalidCredentialsException extends AuthenticationFailedException {
    public InvalidCredentialsException() {
        super("Invalid credentials", "INVALID_CREDENTIALS");
    }
}
