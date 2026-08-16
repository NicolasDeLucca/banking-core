package com.bankingcore.shared.error;

/**
 * Base type for every business/domain error in the system.
 * Intentionally free of any HTTP concept (status codes, headers, etc.):
 * mapping to a transport-level response is a web/infrastructure responsibility,
 * handled by {@link GlobalExceptionHandler}.
 */
public abstract class DomainException extends RuntimeException {

    private final String code;

    protected DomainException(String message, String code) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
