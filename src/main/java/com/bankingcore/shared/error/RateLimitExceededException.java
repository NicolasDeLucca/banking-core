package com.bankingcore.shared.error;

/** The caller has made too many requests of some kind in too short a window (e.g. login attempts). */
public abstract class RateLimitExceededException extends DomainException {
    protected RateLimitExceededException(String message, String code) {
        super(message, code);
    }
}
