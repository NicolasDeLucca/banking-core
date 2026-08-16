package com.bankingcore.account.domain.event;

/**
 * Kind of balance movement an account just went through. Intentionally mirrors
 * transaction.domain.TransactionType without depending on it: account must never
 * import anything from the transaction module (see AccountMovementEvent).
 */
public enum AccountMovementType {
    DEPOSIT,
    WITHDRAW,
    TRANSFER_IN,
    TRANSFER_OUT
}
