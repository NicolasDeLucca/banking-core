package com.bankingcore.account.web.dtos;

public record TransferResponse(AccountResponse source, AccountResponse destination) {
}
