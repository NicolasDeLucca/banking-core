package com.bankingcore.backend.account.application;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.bankingcore.backend.account.domain.Account;
import com.bankingcore.backend.account.domain.AccountRepository;
import com.bankingcore.backend.account.domain.AccountStatus;
import com.bankingcore.backend.account.domain.AccountType;
import com.bankingcore.backend.account.domain.InsufficientFundsException;
import com.bankingcore.backend.account.domain.Money;
import com.bankingcore.backend.account.domain.SameAccountTransferException;
import com.bankingcore.backend.account.domain.UnauthorizedAccountAccessException;
import com.bankingcore.backend.account.domain.event.AccountMovementEvent;
import com.bankingcore.backend.account.domain.event.AccountMovementType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferMoneyUseCaseTest {

    private static final Long REQUESTER_ID = 10L;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private TransferMoneyUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new TransferMoneyUseCase(accountRepository, eventPublisher);
    }

    @Test
    void transfersFundsAndPublishesBothLegsOfTheMovement() {
        Account source = accountWithBalance(1L, REQUESTER_ID, "500");
        Account destination = accountWithBalance(2L, 20L, "100");
        when(accountRepository.findById(1L)).thenReturn(Optional.of(source));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(destination));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TransferResult result = useCase.execute(1L, 2L, REQUESTER_ID, new BigDecimal("150"));

        assertThat(result.source().balance()).isEqualByComparingTo("350.00");
        assertThat(result.destination().balance()).isEqualByComparingTo("250.00");

        ArgumentCaptor<AccountMovementEvent> captor = ArgumentCaptor.forClass(AccountMovementEvent.class);
        verify(eventPublisher, times(2)).publishEvent(captor.capture());

        assertThat(captor.getAllValues()).anySatisfy(event -> {
            assertThat(event.accountId()).isEqualTo(1L);
            assertThat(event.movementType()).isEqualTo(AccountMovementType.TRANSFER_OUT);
            assertThat(event.relatedAccountId()).isEqualTo(2L);
        }).anySatisfy(event -> {
            assertThat(event.accountId()).isEqualTo(2L);
            assertThat(event.movementType()).isEqualTo(AccountMovementType.TRANSFER_IN);
            assertThat(event.relatedAccountId()).isEqualTo(1L);
        });
    }

    @Test
    void rejectsTransferToTheSameAccountWithoutTouchingTheRepository() {
        assertThatThrownBy(() -> useCase.execute(1L, 1L, REQUESTER_ID, BigDecimal.TEN))
                .isInstanceOf(SameAccountTransferException.class);

        verifyNoInteractions(accountRepository);
    }

    @Test
    void rejectsTransferWhenRequesterIsNotTheSourceOwner() {
        Account source = accountWithBalance(1L, REQUESTER_ID, "500");
        when(accountRepository.findById(1L)).thenReturn(Optional.of(source));

        assertThatThrownBy(() -> useCase.execute(1L, 2L, 999L, BigDecimal.TEN))
                .isInstanceOf(UnauthorizedAccountAccessException.class);
    }

    @Test
    void rejectsTransferWithInsufficientFunds() {
        Account source = accountWithBalance(1L, REQUESTER_ID, "5");
        Account destination = accountWithBalance(2L, 20L, "0");
        when(accountRepository.findById(1L)).thenReturn(Optional.of(source));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(destination));

        assertThatThrownBy(() -> useCase.execute(1L, 2L, REQUESTER_ID, new BigDecimal("100")))
                .isInstanceOf(InsufficientFundsException.class);
    }

    private Account accountWithBalance(Long id, Long ownerId, String balance) {
        return Account.reconstitute(id, ownerId, AccountType.CHECKING, Money.of(new BigDecimal(balance)), AccountStatus.ACTIVE);
    }
}
