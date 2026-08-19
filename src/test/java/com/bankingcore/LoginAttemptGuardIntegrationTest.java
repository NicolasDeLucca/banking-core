package com.bankingcore;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Uses a dedicated email not touched by any other test in this suite - the
 * lockout guard is a shared singleton across the whole test context, keyed
 * by email, so reusing an email another test also logs in with could
 * produce order-dependent failures here.
 */
@SpringBootTest
@AutoConfigureMockMvc
class LoginAttemptGuardIntegrationTest {

    private static final String EMAIL = "lockout-target@example.com";
    private static final String PASSWORD = "supersecret123";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void locksOutAfterTooManyFailedLoginAttempts() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + EMAIL + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isCreated());

        // Default app.security.login.max-attempts is 5 - each of these is a
        // plain wrong-password rejection, not yet a lockout.
        for (int attempt = 0; attempt < 5; attempt++) {
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"" + EMAIL + "\",\"password\":\"wrong-password\"}"))
                    .andExpect(status().isUnauthorized());
        }

        // The 6th attempt is rejected by the lockout itself - even with the
        // CORRECT password, proving credentials are never even compared once
        // locked out.
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + EMAIL + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("TOO_MANY_ATTEMPTS"));
    }
}
