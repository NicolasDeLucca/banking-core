package com.bankingcore.account.web.dtos;

import java.math.BigDecimal;

import com.bankingcore.account.domain.AccountStatus;
import com.bankingcore.account.domain.AccountType;

public record AccountResponse(Long id, Long ownerId, AccountType type, BigDecimal balance, AccountStatus status) {
}
