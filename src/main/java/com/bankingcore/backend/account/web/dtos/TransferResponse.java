package com.bankingcore.backend.account.web.dtos;

public record TransferResponse(AccountResponse source, AccountResponse destination) {
}
