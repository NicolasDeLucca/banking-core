package com.bankingcore.transaction.web;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bankingcore.transaction.application.ListAccountTransactionsUseCase;
import com.bankingcore.transaction.application.TransactionResult;
import com.bankingcore.transaction.web.dtos.TransactionResponse;

@RestController
@RequestMapping("/api/accounts/{accountId}/transactions")
public class TransactionController {

    private final ListAccountTransactionsUseCase listAccountTransactionsUseCase;

    public TransactionController(ListAccountTransactionsUseCase listAccountTransactionsUseCase) {
        this.listAccountTransactionsUseCase = listAccountTransactionsUseCase;
    }

    @GetMapping
    public List<TransactionResponse> list(
            @AuthenticationPrincipal Long currentUserId,
            @PathVariable Long accountId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        return listAccountTransactionsUseCase.execute(accountId, currentUserId, page, size).stream()
                .map(this::toResponse)
                .toList();
    }

    private TransactionResponse toResponse(TransactionResult result) {
        return new TransactionResponse(
                result.id(), result.accountId(), result.relatedAccountId(), result.type(), result.amount(), result.occurredAt());
    }
}
