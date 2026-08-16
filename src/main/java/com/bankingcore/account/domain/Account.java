package com.bankingcore.account.domain;

/**
 * Pure domain entity. Owns every rule about its own state (GRASP Information
 * Expert): the balance can only ever change through its own behavior, never
 * via a setter. No dependency on Spring/JPA — see account.infrastructure.
 */
public final class Account {

    private final Long id;
    private final Long ownerId;
    private final AccountType type;
    private Money balance;
    private AccountStatus status;

    private Account(Long id, Long ownerId, AccountType type, Money balance, AccountStatus status) {
        this.id = id;
        this.ownerId = ownerId;
        this.type = type;
        this.balance = balance;
        this.status = status;
    }

    /** Opens a brand-new account for the given owner, always ACTIVE with zero balance. */
    public static Account open(Long ownerId, AccountType type) {
        if (ownerId == null) {
            throw new IllegalArgumentException("ownerId is required");
        }
        if (type == null) {
            throw new IllegalArgumentException("type is required");
        }
        return new Account(null, ownerId, type, Money.zero(), AccountStatus.ACTIVE);
    }

    /** Rebuilds an account coming from persistence. */
    public static Account reconstitute(Long id, Long ownerId, AccountType type, Money balance, AccountStatus status) {
        return new Account(id, ownerId, type, balance, status);
    }

    public void deposit(Money amount) {
        requireOperable();
        requirePositive(amount);
        this.balance = this.balance.add(amount);
    }

    public void withdraw(Money amount) {
        requireOperable();
        requirePositive(amount);
        if (this.balance.isLessThan(amount)) {
            throw new InsufficientFundsException(id);
        }
        this.balance = this.balance.subtract(amount);
    }

    /** Closing is only blocked by CLOSED (terminal) status and a non-zero balance, not by BLOCKED. */
    public void close() {
        requireNotClosed();
        if (!balance.isZero()) {
            throw new AccountHasBalanceException(id);
        }
        this.status = AccountStatus.CLOSED;
    }

    public void block() {
        requireNotClosed();
        this.status = AccountStatus.BLOCKED;
    }

    public void activate() {
        requireNotClosed();
        this.status = AccountStatus.ACTIVE;
    }

    /** Ownership check lives here (not in a service) because Account is the object that knows its owner. */
    public void verifyOwnedBy(Long requesterId) {
        if (!this.ownerId.equals(requesterId)) {
            throw new UnauthorizedAccountAccessException(id);
        }
    }

    private void requireOperable() {
        requireNotClosed();
        if (status == AccountStatus.BLOCKED) {
            throw new AccountBlockedException(id);
        }
    }

    private void requireNotClosed() {
        if (status == AccountStatus.CLOSED) {
            throw new AccountClosedException(id);
        }
    }

    private void requirePositive(Money amount) {
        if (amount == null || !amount.isPositive()) {
            throw new InvalidAmountException();
        }
    }

    public Long getId() {
        return id;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public AccountType getType() {
        return type;
    }

    public Money getBalance() {
        return balance;
    }

    public AccountStatus getStatus() {
        return status;
    }
}
