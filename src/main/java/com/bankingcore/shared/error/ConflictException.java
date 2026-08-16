package com.bankingcore.shared.error;

/** The requested operation conflicts with the current state of a resource (e.g. duplicate email). */
public abstract class ConflictException extends DomainException {
    protected ConflictException(String message, String code) {
        super(message, code);
    }
}
