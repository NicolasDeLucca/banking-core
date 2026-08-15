package com.bankingcore.backend.account.web.dtos;

import java.math.BigDecimal;

import com.bankingcore.backend.account.domain.AccountStatus;
import com.bankingcore.backend.account.domain.AccountType;

public record AccountResponse(Long id, Long ownerId, AccountType type, BigDecimal balance, AccountStatus status) {
}
