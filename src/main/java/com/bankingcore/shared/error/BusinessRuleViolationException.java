package com.bankingcore.shared.error;

/** A request is well-formed but violates a domain business rule (e.g. insufficient funds). */
public abstract class BusinessRuleViolationException extends DomainException {
    protected BusinessRuleViolationException(String message, String code) {
        super(message, code);
    }
}
