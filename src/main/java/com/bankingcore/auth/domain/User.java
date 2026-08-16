package com.bankingcore.auth.domain;

/**
 * Pure domain entity. Deliberately has no dependency on Spring, JPA or any
 * framework: persistence concerns live in auth.infrastructure.persistence
 * (see UserJpaEntity / UserMapper).
 */
public class User {

    private final Long id;
    private final String email;
    private final String passwordHash;
    private final UserRole role;

    private User(Long id, String email, String passwordHash, UserRole role) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    /** Creates a brand-new user about to be registered, always with role USER. */
    public static User register(String email, String passwordHash) {
        return new User(null, normalize(email), passwordHash, UserRole.USER);
    }

    /**
     * Creates an ADMIN user. There is no public API path to this factory — role
     * escalation isn't a self-service feature — it exists solely for bootstrapping
     * the first admin account (see auth.infrastructure.AdminUserSeeder).
     */
    public static User seedAdmin(String email, String passwordHash) {
        return new User(null, normalize(email), passwordHash, UserRole.ADMIN);
    }

    /** Rebuilds a user coming from persistence. */
    public static User reconstitute(Long id, String email, String passwordHash, UserRole role) {
        return new User(id, email, passwordHash, role);
    }

    /** Returns a copy of this user carrying the id assigned by persistence. */
    public User withId(Long assignedId) {
        return new User(assignedId, this.email, this.passwordHash, this.role);
    }

    private static String normalize(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public UserRole getRole() {
        return role;
    }
}
