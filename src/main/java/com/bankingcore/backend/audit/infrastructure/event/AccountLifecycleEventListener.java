package com.bankingcore.backend.audit.infrastructure.event;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.bankingcore.backend.account.domain.event.AccountLifecycleEvent;
import com.bankingcore.backend.audit.application.RecordAuditLogUseCase;

/** Synchronous, same-transaction: only ever published after a successful lifecycle change. */
@Component
public class AccountLifecycleEventListener {

    private final RecordAuditLogUseCase recordAuditLogUseCase;

    public AccountLifecycleEventListener(RecordAuditLogUseCase recordAuditLogUseCase) {
        this.recordAuditLogUseCase = recordAuditLogUseCase;
    }

    @EventListener
    public void on(AccountLifecycleEvent event) {
        recordAuditLogUseCase.execute(
                event.actorUserId(),
                "ACCOUNT_" + event.action().name(),
                "ACCOUNT",
                String.valueOf(event.accountId()),
                null,
                event.occurredAt()
        );
    }
}
