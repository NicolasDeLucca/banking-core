package com.bankingcore.account.domain;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountTest {

    @Test
    void opensWithZeroBalanceAndActiveStatus() {
        Account account = Account.open(1L, AccountType.CHECKING);

        assertThat(account.getBalance()).isEqualTo(Money.zero());
        assertThat(account.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(account.getOwnerId()).isEqualTo(1L);
    }

    @Test
    void openRequiresOwnerAndType() {
        assertThatThrownBy(() -> Account.open(null, AccountType.CHECKING)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Account.open(1L, null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void depositIncreasesBalance() {
        Account account = Account.open(1L, AccountType.CHECKING);

        account.deposit(Money.of(new BigDecimal("100")));

        assertThat(account.getBalance().amount()).isEqualByComparingTo("100.00");
    }

    @Test
    void withdrawDecreasesBalance() {
        Account account = Account.open(1L, AccountType.CHECKING);
        account.deposit(Money.of(new BigDecimal("100")));

        account.withdraw(Money.of(new BigDecimal("40")));

        assertThat(account.getBalance().amount()).isEqualByComparingTo("60.00");
    }

    @Test
    void withdrawFailsWithInsufficientFunds() {
        Account account = Account.open(1L, AccountType.CHECKING);

        assertThatThrownBy(() -> account.withdraw(Money.of(new BigDecimal("1"))))
                .isInstanceOf(InsufficientFundsException.class);
    }

    @Test
    void depositRejectsNonPositiveAmount() {
        Account account = Account.open(1L, AccountType.CHECKING);

        assertThatThrownBy(() -> account.deposit(Money.zero())).isInstanceOf(InvalidAmountException.class);
    }

    @Test
    void closedAccountCannotDepositOrWithdraw() {
        Account account = reconstituteWithStatus(AccountStatus.CLOSED);

        assertThatThrownBy(() -> account.deposit(Money.of(BigDecimal.ONE))).isInstanceOf(AccountClosedException.class);
        assertThatThrownBy(() -> account.withdraw(Money.of(BigDecimal.ONE))).isInstanceOf(AccountClosedException.class);
    }

    @Test
    void blockedAccountCannotDepositOrWithdraw() {
        Account account = reconstituteWithStatus(AccountStatus.BLOCKED);

        assertThatThrownBy(() -> account.deposit(Money.of(BigDecimal.ONE))).isInstanceOf(AccountBlockedException.class);
        assertThatThrownBy(() -> account.withdraw(Money.of(BigDecimal.ONE))).isInstanceOf(AccountBlockedException.class);
    }

    @Test
    void closingWithZeroBalanceSucceeds() {
        Account account = Account.open(1L, AccountType.CHECKING);

        account.close();

        assertThat(account.getStatus()).isEqualTo(AccountStatus.CLOSED);
    }

    @Test
    void closingWithNonZeroBalanceFails() {
        Account account = Account.open(1L, AccountType.CHECKING);
        account.deposit(Money.of(BigDecimal.TEN));

        assertThatThrownBy(account::close).isInstanceOf(AccountHasBalanceException.class);
    }

    @Test
    void closingAnAlreadyClosedAccountFails() {
        Account account = Account.open(1L, AccountType.CHECKING);
        account.close();

        assertThatThrownBy(account::close).isInstanceOf(AccountClosedException.class);
    }

    @Test
    void blockedAccountCanStillBeClosedIfBalanceIsZero() {
        Account account = reconstituteWithStatus(AccountStatus.BLOCKED);

        account.close();

        assertThat(account.getStatus()).isEqualTo(AccountStatus.CLOSED);
    }

    @Test
    void closedAccountCannotBeReactivatedOrBlocked() {
        Account account = reconstituteWithStatus(AccountStatus.CLOSED);

        assertThatThrownBy(account::activate).isInstanceOf(AccountClosedException.class);
        assertThatThrownBy(account::block).isInstanceOf(AccountClosedException.class);
    }

    @Test
    void verifyOwnedByRejectsOtherUsers() {
        Account account = Account.open(1L, AccountType.CHECKING);

        assertThatThrownBy(() -> account.verifyOwnedBy(2L)).isInstanceOf(UnauthorizedAccountAccessException.class);

        account.verifyOwnedBy(1L); // does not throw for the actual owner
    }

    private Account reconstituteWithStatus(AccountStatus status) {
        return Account.reconstitute(1L, 1L, AccountType.CHECKING, Money.zero(), status);
    }
}
