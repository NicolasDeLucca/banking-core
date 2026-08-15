package com.bankingcore.backend.shared.error;

/** A requested resource does not exist. */
public abstract class NotFoundException extends DomainException {
    protected NotFoundException(String message, String code) {
        super(message, code);
    }
}
