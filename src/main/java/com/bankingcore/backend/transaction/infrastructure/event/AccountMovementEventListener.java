package com.bankingcore.backend.transaction.infrastructure.event;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.bankingcore.backend.account.domain.event.AccountMovementEvent;
import com.bankingcore.backend.account.domain.event.AccountMovementType;
import com.bankingcore.backend.transaction.application.RecordTransactionUseCase;
import com.bankingcore.backend.transaction.domain.TransactionType;

/**
 * Reacts to account balance changes and turns each one into a ledger entry.
 * Runs synchronously, inside the same transaction as the account operation that
 * published the event: if recording fails, the whole operation rolls back too.
 */
@Component
public class AccountMovementEventListener {

    private final RecordTransactionUseCase recordTransactionUseCase;

    public AccountMovementEventListener(RecordTransactionUseCase recordTransactionUseCase) {
        this.recordTransactionUseCase = recordTransactionUseCase;
    }

    @EventListener
    public void on(AccountMovementEvent event) {
        recordTransactionUseCase.execute(
                event.accountId(),
                event.relatedAccountId(),
                toTransactionType(event.movementType()),
                event.amount(),
                event.occurredAt()
        );
    }

    private TransactionType toTransactionType(AccountMovementType movementType) {
        return switch (movementType) {
            case DEPOSIT -> TransactionType.DEPOSIT;
            case WITHDRAW -> TransactionType.WITHDRAW;
            case TRANSFER_IN -> TransactionType.TRANSFER_IN;
            case TRANSFER_OUT -> TransactionType.TRANSFER_OUT;
        };
    }
}
