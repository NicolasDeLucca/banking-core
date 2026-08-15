package com.bankingcore.backend.account.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.bankingcore.backend.account.application.AccountResult;
import com.bankingcore.backend.account.application.CloseAccountUseCase;
import com.bankingcore.backend.account.application.CreateAccountUseCase;
import com.bankingcore.backend.account.application.DepositMoneyUseCase;
import com.bankingcore.backend.account.application.GetAccountDetailsUseCase;
import com.bankingcore.backend.account.application.ListUserAccountsUseCase;
import com.bankingcore.backend.account.application.TransferMoneyUseCase;
import com.bankingcore.backend.account.application.TransferResult;
import com.bankingcore.backend.account.application.WithdrawMoneyUseCase;
import com.bankingcore.backend.account.web.dtos.AccountResponse;
import com.bankingcore.backend.account.web.dtos.AmountRequest;
import com.bankingcore.backend.account.web.dtos.CreateAccountRequest;
import com.bankingcore.backend.account.web.dtos.TransferRequest;
import com.bankingcore.backend.account.web.dtos.TransferResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final CreateAccountUseCase createAccountUseCase;
    private final ListUserAccountsUseCase listUserAccountsUseCase;
    private final GetAccountDetailsUseCase getAccountDetailsUseCase;
    private final DepositMoneyUseCase depositMoneyUseCase;
    private final WithdrawMoneyUseCase withdrawMoneyUseCase;
    private final CloseAccountUseCase closeAccountUseCase;
    private final TransferMoneyUseCase transferMoneyUseCase;

    public AccountController(
            CreateAccountUseCase createAccountUseCase,
            ListUserAccountsUseCase listUserAccountsUseCase,
            GetAccountDetailsUseCase getAccountDetailsUseCase,
            DepositMoneyUseCase depositMoneyUseCase,
            WithdrawMoneyUseCase withdrawMoneyUseCase,
            CloseAccountUseCase closeAccountUseCase,
            TransferMoneyUseCase transferMoneyUseCase
    ) {
        this.createAccountUseCase = createAccountUseCase;
        this.listUserAccountsUseCase = listUserAccountsUseCase;
        this.getAccountDetailsUseCase = getAccountDetailsUseCase;
        this.depositMoneyUseCase = depositMoneyUseCase;
        this.withdrawMoneyUseCase = withdrawMoneyUseCase;
        this.closeAccountUseCase = closeAccountUseCase;
        this.transferMoneyUseCase = transferMoneyUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse create(@AuthenticationPrincipal Long currentUserId, @Valid @RequestBody CreateAccountRequest request) {
        return toResponse(createAccountUseCase.execute(currentUserId, request.type()));
    }

    @GetMapping
    public List<AccountResponse> listMine(@AuthenticationPrincipal Long currentUserId) {
        return listUserAccountsUseCase.execute(currentUserId).stream().map(this::toResponse).toList();
    }

    @GetMapping("/{accountId}")
    public AccountResponse getById(@AuthenticationPrincipal Long currentUserId, @PathVariable Long accountId) {
        return toResponse(getAccountDetailsUseCase.execute(accountId, currentUserId));
    }

    @PostMapping("/{accountId}/deposit")
    public AccountResponse deposit(
            @AuthenticationPrincipal Long currentUserId,
            @PathVariable Long accountId,
            @Valid @RequestBody AmountRequest request
    ) {
        return toResponse(depositMoneyUseCase.execute(accountId, currentUserId, request.amount()));
    }

    @PostMapping("/{accountId}/withdraw")
    public AccountResponse withdraw(
            @AuthenticationPrincipal Long currentUserId,
            @PathVariable Long accountId,
            @Valid @RequestBody AmountRequest request
    ) {
        return toResponse(withdrawMoneyUseCase.execute(accountId, currentUserId, request.amount()));
    }

    @PostMapping("/{accountId}/close")
    public AccountResponse close(@AuthenticationPrincipal Long currentUserId, @PathVariable Long accountId) {
        return toResponse(closeAccountUseCase.execute(accountId, currentUserId));
    }

    @PostMapping("/{accountId}/transfer")
    public TransferResponse transfer(
            @AuthenticationPrincipal Long currentUserId,
            @PathVariable Long accountId,
            @Valid @RequestBody TransferRequest request
    ) {
        TransferResult result = transferMoneyUseCase.execute(accountId, request.destinationAccountId(), currentUserId, request.amount());
        return new TransferResponse(toResponse(result.source()), toResponse(result.destination()));
    }

    private AccountResponse toResponse(AccountResult result) {
        return new AccountResponse(result.id(), result.ownerId(), result.type(), result.balance(), result.status());
    }
}
