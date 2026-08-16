package com.bankingcore.shared.error;

/** The caller could not be authenticated (e.g. invalid credentials or token). */
public abstract class AuthenticationFailedException extends DomainException {
    protected AuthenticationFailedException(String message, String code) {
        super(message, code);
    }
}
