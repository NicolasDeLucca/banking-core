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
import com.bankingcore.account.domain.AccountClosedException;
import com.bankingcore.account.domain.AccountHasBalanceException;
import com.bankingcore.account.domain.AccountNotFoundException;
import com.bankingcore.account.domain.AccountRepository;
import com.bankingcore.account.domain.AccountStatus;
import com.bankingcore.account.domain.AccountType;
import com.bankingcore.account.domain.Money;
import com.bankingcore.account.domain.UnauthorizedAccountAccessException;
import com.bankingcore.account.domain.event.AccountLifecycleAction;
import com.bankingcore.account.domain.event.AccountLifecycleEvent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CloseAccountUseCaseTest {

    private static final Long ACCOUNT_ID = 1L;
    private static final Long OWNER_ID = 10L;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private CloseAccountUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CloseAccountUseCase(accountRepository, eventPublisher);
    }

    @Test
    void closesAZeroBalanceAccountAndPublishesALifecycleEvent() {
        Account account = accountWithBalanceAndStatus("0", AccountStatus.ACTIVE);
        when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AccountResult result = useCase.execute(ACCOUNT_ID, OWNER_ID);

        assertThat(result.status()).isEqualTo(AccountStatus.CLOSED);

        ArgumentCaptor<AccountLifecycleEvent> captor = ArgumentCaptor.forClass(AccountLifecycleEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().accountId()).isEqualTo(ACCOUNT_ID);
        assertThat(captor.getValue().actorUserId()).isEqualTo(OWNER_ID);
        assertThat(captor.getValue().action()).isEqualTo(AccountLifecycleAction.CLOSED);
    }

    @Test
    void rejectsWhenAccountDoesNotExist() {
        when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(ACCOUNT_ID, OWNER_ID))
                .isInstanceOf(AccountNotFoundException.class);

        verifyNoInteractions(eventPublisher);
    }

    @Test
    void rejectsWhenRequesterIsNotTheOwner() {
        Account account = accountWithBalanceAndStatus("0", AccountStatus.ACTIVE);
        when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> useCase.execute(ACCOUNT_ID, 999L))
                .isInstanceOf(UnauthorizedAccountAccessException.class);
    }

    @Test
    void rejectsClosingAnAccountWithANonZeroBalance() {
        Account account = accountWithBalanceAndStatus("50", AccountStatus.ACTIVE);
        when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> useCase.execute(ACCOUNT_ID, OWNER_ID))
                .isInstanceOf(AccountHasBalanceException.class);
    }

    @Test
    void rejectsClosingAnAlreadyClosedAccount() {
        Account account = accountWithBalanceAndStatus("0", AccountStatus.CLOSED);
        when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> useCase.execute(ACCOUNT_ID, OWNER_ID))
                .isInstanceOf(AccountClosedException.class);
    }

    @Test
    void aBlockedAccountCanStillBeClosed() {
        // Account.close() is only blocked by CLOSED status and a non-zero balance,
        // not by BLOCKED - a blocked account with a zero balance is closeable.
        Account account = accountWithBalanceAndStatus("0", AccountStatus.BLOCKED);
        when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AccountResult result = useCase.execute(ACCOUNT_ID, OWNER_ID);

        assertThat(result.status()).isEqualTo(AccountStatus.CLOSED);
    }

    private Account accountWithBalanceAndStatus(String balance, AccountStatus status) {
        return Account.reconstitute(ACCOUNT_ID, OWNER_ID, AccountType.CHECKING, Money.of(new BigDecimal(balance)), status);
    }
}
