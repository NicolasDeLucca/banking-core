package com.bankingcore.shared.error;

/** The caller is authenticated but not allowed to perform the requested operation. */
public abstract class ForbiddenOperationException extends DomainException {
    protected ForbiddenOperationException(String message, String code) {
        super(message, code);
    }
}
