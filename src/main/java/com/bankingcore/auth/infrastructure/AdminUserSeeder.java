package com.bankingcore.auth.infrastructure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.bankingcore.auth.application.PasswordHasher;
import com.bankingcore.auth.domain.User;
import com.bankingcore.auth.domain.UserRepository;

/**
 * Bootstraps a single ADMIN account on startup if none exists yet. There is no
 * public API to create an ADMIN (role escalation isn't a self-service feature),
 * so this is the only way the first admin gets provisioned.
 */
@Component
public class AdminUserSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminUserSeeder.class);

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final String adminEmail;
    private final String adminPassword;

    public AdminUserSeeder(
            UserRepository userRepository,
            PasswordHasher passwordHasher,
            @Value("${app.admin.email}") String adminEmail,
            @Value("${app.admin.password}") String adminPassword
    ) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.existsByEmail(adminEmail)) {
            return;
        }
        userRepository.save(User.seedAdmin(adminEmail, passwordHasher.hash(adminPassword)));
        log.warn("Seeded default ADMIN account ({}). Change its password before using this outside development.", adminEmail);
    }
}
