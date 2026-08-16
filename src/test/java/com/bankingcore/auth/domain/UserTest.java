package com.bankingcore.auth.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    void registerNormalizesEmailAndDefaultsToUserRole() {
        User user = User.register("  Test@Example.COM  ", "hashed");

        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getRole()).isEqualTo(UserRole.USER);
        assertThat(user.getId()).isNull();
    }

    @Test
    void withIdReturnsCopyCarryingTheAssignedId() {
        User user = User.register("test@example.com", "hashed");

        User withId = user.withId(42L);

        assertThat(withId.getId()).isEqualTo(42L);
        assertThat(user.getId()).isNull(); // original instance is untouched (immutable)
    }
}
