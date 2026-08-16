package com.bankingcore.auth.domain;

import com.bankingcore.shared.error.ConflictException;

public class EmailAlreadyRegisteredException extends ConflictException {
    public EmailAlreadyRegisteredException(String email) {
        super("Email already registered: " + email, "EMAIL_ALREADY_EXISTS");
    }
}
