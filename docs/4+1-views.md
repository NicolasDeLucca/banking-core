# 4+1 Architectural Views

Kruchten's 4+1 model, documenting this project as it's actually built today
— not an aspirational target. Each view answers a different question about
the same system; together they cover it without any single diagram trying
to show everything at once.

## 1. Use Case View

**Actors:** `USER`, `ADMIN` (see [Security](../README.md#security) for how
a request gets tied to one of these).

```mermaid
flowchart LR
    USER((USER))
    ADMIN((ADMIN))

    subgraph "Self-service (auth + account)"
        UC1[Register]
        UC2[Login]
        UC3[Create account]
        UC4[Deposit]
        UC5[Withdraw]
        UC6[Transfer]
        UC7[Close account]
        UC8[View own account]
        UC9[View own transactions]
    end

    subgraph "Administrative"
        UC10[List / view any account]
        UC11[Block / activate / close any account]
        UC12[View audit trail]
    end

    USER --> UC1 & UC2 & UC3 & UC4 & UC5 & UC6 & UC7 & UC8 & UC9
    ADMIN --> UC2
    ADMIN --> UC10 & UC11 & UC12
```

`ADMIN` only participates in Login and the administrative group — there's
no self-service path to becoming an admin (see
[`AdminUserSeeder`](../src/main/java/com/bankingcore/auth/infrastructure/AdminUserSeeder.java)).

## 2. Logical View

The domain model. `Money` is a value object, not an entity — it has no
identity of its own, only a value.

```mermaid
classDiagram
    class User {
        Long id
        String email
        String passwordHash
        UserRole role
        +register(email, passwordHash)$ User
        +seedAdmin(email, passwordHash)$ User
    }
    class UserRole {
        <<enumeration>>
        USER
        ADMIN
    }
    class Account {
        Long id
        Long ownerId
        AccountType type
        Money balance
        AccountStatus status
        +deposit(Money amount)
        +withdraw(Money amount)
        +close()
        +block()
        +activate()
        +verifyOwnedBy(Long requesterId)
    }
    class AccountStatus {
        <<enumeration>>
        ACTIVE
        BLOCKED
        CLOSED
    }
    class Money {
        <<value object>>
        BigDecimal amount
        +add(Money) Money
        +subtract(Money) Money
    }
    class Transaction {
        Long id
        Long accountId
        Long relatedAccountId
        TransactionType type
        BigDecimal amount
        Instant occurredAt
    }
    class TransactionType {
        <<enumeration>>
        DEPOSIT
        WITHDRAW
        TRANSFER_IN
        TRANSFER_OUT
    }
    class AuditLog {
        Long id
        Long actorUserId
        String action
        String targetType
        String targetId
        Instant occurredAt
    }

    User "1" --> "0..*" Account : owns (ownerId)
    Account "1" --> "0..*" Transaction : ledger entries (accountId)
    Account "1" o-- "1" Money : balance
    Account --> AccountStatus
    User --> UserRole
    Transaction --> TransactionType
```

`AuditLog` isn't linked by a foreign key to `User`/`Account` in this
diagram on purpose: it references them only by plain id (`actorUserId`,
`targetId`), the same way the `audit` module only ever depends on
`auth`/`account`'s published events, never their entities directly (see
the [Development View](#3-development-view)).

**Value objects considered but not built:** `AccountId`/`UserId`/
`TransactionId` as wrapper types over `Long`, and `Email` as a wrapper
over `String`. Skipped — nothing in this codebase today branches on their
type in a way a plain `Long`/`String` couldn't already do just as safely.

## 3. Development View

Represented by the module boundaries themselves — see the
[Architecture](../README.md#architecture) section for what each layer
inside a module is responsible for. What matters most here is the
*direction* of dependency between modules:

```mermaid
flowchart TD
    subgraph auth
        authD[domain] --> authA[application] --> authI[infrastructure]
        authI --> authW[web]
    end
    subgraph account
        accD[domain] --> accA[application] --> accI[infrastructure]
        accI --> accW[web]
    end
    subgraph transaction
        txD[domain] --> txA[application] --> txI[infrastructure]
    end
    subgraph audit
        auD[domain] --> auA[application] --> auI[infrastructure]
    end

    txA -.->|reads AccountRepository<br/>for ownership checks| accD
    txI -.->|listens to| accD
    auI -.->|listens to| accD
    auI -.->|listens to| authD
```

`account` has no dependency arrow pointing *into* `transaction` or
`audit` — it publishes events without knowing who, if anyone, reacts to
them. This is deliberate: it's the Acyclic Dependencies Principle applied
at the module level, and it's what lets `audit` exist as a "sink" module
that nothing else needs to know about.

## 4. Process View

Concurrency and cross-module coordination matter more than any single
class here — the four flows below are the ones worth tracing end to end.

### Login (and why a failed attempt still gets audited)

```mermaid
sequenceDiagram
    participant C as Client
    participant AC as AuthController
    participant LU as LoginUserUseCase
    participant TP as TokenProvider
    participant AL as audit listener<br/>(AFTER_COMPLETION)

    C->>AC: POST /api/auth/login
    AC->>LU: execute(email, password)
    LU->>LU: publish UserAuthenticationEvent
    alt credentials valid
        LU->>TP: generateToken(id, email, role)
        LU-->>AC: AuthResult(token)
        AC-->>C: 200 + token
    else invalid
        LU-->>AC: throw InvalidCredentialsException
        AC-->>C: 401 INVALID_CREDENTIALS
    end
    Note over LU,AL: The event is published before the possible throw.<br/>Its listener runs AFTER_COMPLETION, in its own<br/>transaction, so LOGIN_FAILURE is recorded even<br/>though the login transaction itself rolled back.
```

### Transfer (atomic across two accounts + the ledger)

```mermaid
sequenceDiagram
    participant C as Client
    participant AC as AccountController
    participant TU as TransferMoneyUseCase
    participant AR as AccountRepository
    participant EP as ApplicationEventPublisher
    participant TL as transaction listener

    C->>AC: POST /accounts/{id}/transfer
    AC->>TU: execute(sourceId, destId, requesterId, amount)
    TU->>AR: findById(source) + verifyOwnedBy(requesterId)
    TU->>AR: findById(destination)
    TU->>TU: source.withdraw(amount)
    TU->>TU: destination.deposit(amount)
    TU->>AR: save(source), save(destination)
    TU->>EP: publish TRANSFER_OUT, TRANSFER_IN
    EP->>TL: record both ledger entries (same transaction)
    TU-->>AC: TransferResult
    AC-->>C: 200 + both updated balances
    Note over TU,TL: One database transaction start to finish.<br/>If the ledger write fails, the whole transfer rolls back.
```

### Concurrent withdrawals on the same account (optimistic locking)

```mermaid
sequenceDiagram
    participant A as Request A
    participant B as Request B
    participant DB as accounts row (version=5)

    A->>DB: SELECT ... (reads version=5)
    B->>DB: SELECT ... (reads version=5)
    A->>DB: UPDATE ... WHERE id=? AND version=5
    DB-->>A: 1 row updated -> commit (version becomes 6)
    B->>DB: UPDATE ... WHERE id=? AND version=5
    DB-->>B: 0 rows updated (version isn't 5 anymore)
    B-->>B: OptimisticLockingFailureException
    Note over B: Mapped to HTTP 409 CONCURRENT_MODIFICATION.<br/>The client is expected to retry the request.
```

### Audit trail (one-directional listener, no cycle back to `account`)

```mermaid
sequenceDiagram
    participant Acc as account module
    participant EP as ApplicationEventPublisher
    participant Au as audit listener
    participant DB as audit_logs

    Acc->>Acc: account.close() succeeds, saved
    Acc->>EP: publish AccountLifecycleEvent(CLOSED)
    EP->>Au: AccountLifecycleEventListener.on(event)
    Au->>DB: INSERT audit_logs (same transaction)
    Note over Acc,DB: account never imports anything from audit.
```

## 5. Physical View

```mermaid
flowchart TD
    Client[HTTP client<br/>Postman / browser / frontend]

    subgraph "Docker Compose network"
        App["banking-core-app<br/>(Spring Boot, :8080)"]
        DB[("banking-core-db<br/>(PostgreSQL 16, :5432)")]
    end

    Client -->|HTTP + Bearer JWT| App
    App -->|JDBC| DB
```

Today this is a single deployable talking to a single database instance —
intentionally simple, matching the [Configuration](../README.md#configuration)
and [Running it](../README.md#running-it) sections of the README. The
modular monolith structure (see the [Architecture](../README.md#architecture)
section) is what would let this evolve toward a different physical
topology later — e.g. separate deployables per module behind a gateway —
without a domain-layer rewrite, but that evolution hasn't happened and
isn't claimed here.
