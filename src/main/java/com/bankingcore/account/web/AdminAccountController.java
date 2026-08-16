package com.bankingcore.account.web;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bankingcore.account.application.AccountResult;
import com.bankingcore.account.application.AdminActivateAccountUseCase;
import com.bankingcore.account.application.AdminBlockAccountUseCase;
import com.bankingcore.account.application.AdminCloseAccountUseCase;
import com.bankingcore.account.application.AdminGetAccountUseCase;
import com.bankingcore.account.application.AdminListAllAccountsUseCase;
import com.bankingcore.account.web.dtos.AccountResponse;

/**
 * Authorization for every endpoint here is enforced at the route level
 * (see config.SecurityConfig: "/api/admin/**" requires ROLE_ADMIN) rather than
 * inside these use cases, keeping domain rules and authorization concerns apart.
 */
@RestController
@RequestMapping("/api/admin/accounts")
public class AdminAccountController {

    private final AdminListAllAccountsUseCase adminListAllAccountsUseCase;
    private final AdminGetAccountUseCase adminGetAccountUseCase;
    private final AdminBlockAccountUseCase adminBlockAccountUseCase;
    private final AdminActivateAccountUseCase adminActivateAccountUseCase;
    private final AdminCloseAccountUseCase adminCloseAccountUseCase;

    public AdminAccountController(
            AdminListAllAccountsUseCase adminListAllAccountsUseCase,
            AdminGetAccountUseCase adminGetAccountUseCase,
            AdminBlockAccountUseCase adminBlockAccountUseCase,
            AdminActivateAccountUseCase adminActivateAccountUseCase,
            AdminCloseAccountUseCase adminCloseAccountUseCase
    ) {
        this.adminListAllAccountsUseCase = adminListAllAccountsUseCase;
        this.adminGetAccountUseCase = adminGetAccountUseCase;
        this.adminBlockAccountUseCase = adminBlockAccountUseCase;
        this.adminActivateAccountUseCase = adminActivateAccountUseCase;
        this.adminCloseAccountUseCase = adminCloseAccountUseCase;
    }

    @GetMapping
    public List<AccountResponse> listAll() {
        return adminListAllAccountsUseCase.execute().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{accountId}")
    public AccountResponse getById(@PathVariable Long accountId) {
        return toResponse(adminGetAccountUseCase.execute(accountId));
    }

    @PostMapping("/{accountId}/block")
    public AccountResponse block(@AuthenticationPrincipal Long adminUserId, @PathVariable Long accountId) {
        return toResponse(adminBlockAccountUseCase.execute(accountId, adminUserId));
    }

    @PostMapping("/{accountId}/activate")
    public AccountResponse activate(@AuthenticationPrincipal Long adminUserId, @PathVariable Long accountId) {
        return toResponse(adminActivateAccountUseCase.execute(accountId, adminUserId));
    }

    @PostMapping("/{accountId}/close")
    public AccountResponse close(@AuthenticationPrincipal Long adminUserId, @PathVariable Long accountId) {
        return toResponse(adminCloseAccountUseCase.execute(accountId, adminUserId));
    }

    private AccountResponse toResponse(AccountResult result) {
        return new AccountResponse(result.id(), result.ownerId(), result.type(), result.balance(), result.status());
    }
}
