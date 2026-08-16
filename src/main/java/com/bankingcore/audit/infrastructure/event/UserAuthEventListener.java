package com.bankingcore.audit.infrastructure.event;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.bankingcore.audit.application.RecordAuditLogUseCase;
import com.bankingcore.auth.domain.event.UserAuthenticationEvent;
import com.bankingcore.auth.domain.event.UserRegisteredEvent;

@Component
public class UserAuthEventListener {

    private final RecordAuditLogUseCase recordAuditLogUseCase;

    public UserAuthEventListener(RecordAuditLogUseCase recordAuditLogUseCase) {
        this.recordAuditLogUseCase = recordAuditLogUseCase;
    }

    @EventListener
    public void onRegistered(UserRegisteredEvent event) {
        recordAuditLogUseCase.execute(
                event.userId(), "USER_REGISTERED", "USER", String.valueOf(event.userId()),
                "User registered with email " + event.email(), event.occurredAt());
    }

    /**
     * AFTER_COMPLETION (not a plain synchronous @EventListener) so a failed login
     * attempt is still recorded even though the transaction that published it
     * rolls back right after (see LoginUserUseCase).
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION)
    public void onAuthenticationAttempt(UserAuthenticationEvent event) {
        String action = event.successful() ? "LOGIN_SUCCESS" : "LOGIN_FAILURE";
        String targetId = event.userId() != null ? String.valueOf(event.userId()) : null;
        recordAuditLogUseCase.execute(
                event.userId(), action, "USER", targetId, "Login attempt for " + event.email(), event.occurredAt());
    }
}
