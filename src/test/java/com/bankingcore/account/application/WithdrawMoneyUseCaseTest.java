package com.bankingcore.account.application;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.bankingcore.account.domain.Account;
import com.bankingcore.account.domain.AccountBlockedException;
import com.bankingcore.account.domain.AccountClosedException;
import com.bankingcore.account.domain.AccountNotFoundException;
import com.bankingcore.account.domain.AccountRepository;
import com.bankingcore.account.domain.AccountStatus;
import com.bankingcore.account.domain.AccountType;
import com.bankingcore.account.domain.InsufficientFundsException;
import com.bankingcore.account.domain.Money;
import com.bankingcore.account.domain.UnauthorizedAccountAccessException;
import com.bankingcore.account.domain.event.AccountMovementEvent;
import com.bankingcore.account.domain.event.AccountMovementType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WithdrawMoneyUseCaseTest {

    private static final Long ACCOUNT_ID = 1L;
    private static final Long OWNER_ID = 10L;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private WithdrawMoneyUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new WithdrawMoneyUseCase(accountRepository, eventPublisher);
    }

    @Test
    void withdrawsAndPublishesAMovementEvent() {
        Account account = accountWithBalanceAndStatus("500", AccountStatus.ACTIVE);
        when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AccountResult result = useCase.execute(ACCOUNT_ID, OWNER_ID, new BigDecimal("150"));

        assertThat(result.balance()).isEqualByComparingTo("350.00");

        ArgumentCaptor<AccountMovementEvent> captor = ArgumentCaptor.forClass(AccountMovementEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().accountId()).isEqualTo(ACCOUNT_ID);
        assertThat(captor.getValue().movementType()).isEqualTo(AccountMovementType.WITHDRAW);
    }

    @Test
    void rejectsWhenAccountDoesNotExist() {
        when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(ACCOUNT_ID, OWNER_ID, BigDecimal.TEN))
                .isInstanceOf(AccountNotFoundException.class);

        verifyNoInteractions(eventPublisher);
    }

    @Test
    void rejectsWhenRequesterIsNotTheOwner() {
        Account account = accountWithBalanceAndStatus("500", AccountStatus.ACTIVE);
        when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> useCase.execute(ACCOUNT_ID, 999L, BigDecimal.TEN))
                .isInstanceOf(UnauthorizedAccountAccessException.class);
    }

    @Test
    void rejectsWithdrawalPastTheBalance() {
        Account account = accountWithBalanceAndStatus("5", AccountStatus.ACTIVE);
        when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> useCase.execute(ACCOUNT_ID, OWNER_ID, new BigDecimal("100")))
                .isInstanceOf(InsufficientFundsException.class);
    }

    @Test
    void rejectsWithdrawalOnAClosedAccount() {
        Account account = accountWithBalanceAndStatus("0", AccountStatus.CLOSED);
        when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> useCase.execute(ACCOUNT_ID, OWNER_ID, BigDecimal.TEN))
                .isInstanceOf(AccountClosedException.class);
    }

    @Test
    void rejectsWithdrawalOnABlockedAccount() {
        Account account = accountWithBalanceAndStatus("500", AccountStatus.BLOCKED);
        when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> useCase.execute(ACCOUNT_ID, OWNER_ID, BigDecimal.TEN))
                .isInstanceOf(AccountBlockedException.class);
    }

    private Account accountWithBalanceAndStatus(String balance, AccountStatus status) {
        return Account.reconstitute(ACCOUNT_ID, OWNER_ID, AccountType.CHECKING, Money.of(new BigDecimal(balance)), status);
    }
}
