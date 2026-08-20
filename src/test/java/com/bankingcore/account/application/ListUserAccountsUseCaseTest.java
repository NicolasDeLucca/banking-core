package com.bankingcore.account.application;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bankingcore.account.domain.Account;
import com.bankingcore.account.domain.AccountRepository;
import com.bankingcore.account.domain.AccountStatus;
import com.bankingcore.account.domain.AccountType;
import com.bankingcore.account.domain.Money;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListUserAccountsUseCaseTest {

    private static final Long OWNER_ID = 10L;

    @Mock
    private AccountRepository accountRepository;

    private ListUserAccountsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ListUserAccountsUseCase(accountRepository);
    }

    @Test
    void listsOnlyTheOwnersAccounts() {
        Account checking = Account.reconstitute(1L, OWNER_ID, AccountType.CHECKING, Money.zero(), AccountStatus.ACTIVE);
        Account savings = Account.reconstitute(2L, OWNER_ID, AccountType.SAVINGS, Money.zero(), AccountStatus.ACTIVE);
        when(accountRepository.findAllByOwnerId(OWNER_ID)).thenReturn(List.of(checking, savings));

        List<AccountResult> results = useCase.execute(OWNER_ID);

        assertThat(results).extracting(AccountResult::id).containsExactly(1L, 2L);
    }

    @Test
    void returnsAnEmptyListWhenTheOwnerHasNoAccounts() {
        when(accountRepository.findAllByOwnerId(OWNER_ID)).thenReturn(List.of());

        assertThat(useCase.execute(OWNER_ID)).isEmpty();
    }
}
