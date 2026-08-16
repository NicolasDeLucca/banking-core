package com.bankingcore.account.infrastructure.persistence;

import java.math.BigDecimal;

import com.bankingcore.account.domain.AccountStatus;
import com.bankingcore.account.domain.AccountType;
import jakarta.persistence.*;

@Entity
@Table(name = "accounts")
public class AccountJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long ownerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private AccountType type;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status;

    /** Optimistic locking: protects concurrent balance mutations (concurrent deposits/withdrawals/transfers). */
    @Version
    private Long version;

    protected AccountJpaEntity() {
        // required by JPA
    }

    public AccountJpaEntity(Long id, Long ownerId, AccountType type, BigDecimal balance, AccountStatus status) {
        this.id = id;
        this.ownerId = ownerId;
        this.type = type;
        this.balance = balance;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public AccountType getType() {
        return type;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }
}
