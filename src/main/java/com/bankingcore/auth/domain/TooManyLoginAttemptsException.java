package com.bankingcore.auth.domain;

import com.bankingcore.shared.error.RateLimitExceededException;

public class TooManyLoginAttemptsException extends RateLimitExceededException {
    public TooManyLoginAttemptsException() {
        super("Too many failed login attempts, try again later", "TOO_MANY_ATTEMPTS");
    }
}
