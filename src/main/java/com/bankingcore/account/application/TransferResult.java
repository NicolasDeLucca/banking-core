package com.bankingcore.account.application;

/** Outcome of a transfer: both accounts as they ended up, so the caller can show updated balances for each side. */
public record TransferResult(AccountResult source, AccountResult destination) {
}
