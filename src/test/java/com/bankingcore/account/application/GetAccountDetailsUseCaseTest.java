package com.bankingcore.account.application;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bankingcore.account.domain.Account;
import com.bankingcore.account.domain.AccountNotFoundException;
import com.bankingcore.account.domain.AccountRepository;
import com.bankingcore.account.domain.AccountStatus;
import com.bankingcore.account.domain.AccountType;
import com.bankingcore.account.domain.Money;
import com.bankingcore.account.domain.UnauthorizedAccountAccessException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAccountDetailsUseCaseTest {

    private static final Long ACCOUNT_ID = 1L;
    private static final Long OWNER_ID = 10L;

    @Mock
    private AccountRepository accountRepository;

    private GetAccountDetailsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetAccountDetailsUseCase(accountRepository);
    }

    @Test
    void returnsTheAccountWhenTheRequesterOwnsIt() {
        Account account = Account.reconstitute(ACCOUNT_ID, OWNER_ID, AccountType.SAVINGS, Money.of(new BigDecimal("42")), AccountStatus.ACTIVE);
        when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(account));

        AccountResult result = useCase.execute(ACCOUNT_ID, OWNER_ID);

        assertThat(result.id()).isEqualTo(ACCOUNT_ID);
        assertThat(result.balance()).isEqualByComparingTo("42");
    }

    @Test
    void rejectsWhenAccountDoesNotExist() {
        when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(ACCOUNT_ID, OWNER_ID))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void rejectsWhenRequesterIsNotTheOwner() {
        Account account = Account.reconstitute(ACCOUNT_ID, OWNER_ID, AccountType.SAVINGS, Money.zero(), AccountStatus.ACTIVE);
        when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> useCase.execute(ACCOUNT_ID, 999L))
                .isInstanceOf(UnauthorizedAccountAccessException.class);
    }
}
