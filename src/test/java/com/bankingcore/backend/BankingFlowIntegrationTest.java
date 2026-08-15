package com.bankingcore.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Exercises the whole stack (security filter chain, controllers, use cases, JPA,
 * the account -> transaction event) the same way the manual curl smoke tests did
 * during development, but as an automated regression suite.
 */
@SpringBootTest
@AutoConfigureMockMvc
class BankingFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void fullBankingFlow() throws Exception {
        String aliceToken = registerAndGetToken("alice-flow@example.com");
        String bobToken = registerAndGetToken("bob-flow@example.com");

        Long aliceAccountId = createAccount(aliceToken, "CHECKING");
        Long bobAccountId = createAccount(bobToken, "SAVINGS");

        deposit(aliceToken, aliceAccountId, "1000");

        mockMvc.perform(post("/api/accounts/{id}/transfer", aliceAccountId)
                        .header("Authorization", "Bearer " + aliceToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"destinationAccountId\":" + bobAccountId + ",\"amount\":300}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.source.balance").value(700.00))
                .andExpect(jsonPath("$.destination.balance").value(300.00));

        mockMvc.perform(post("/api/accounts/{id}/withdraw", aliceAccountId)
                        .header("Authorization", "Bearer " + aliceToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":10000}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("INSUFFICIENT_FUNDS"));

        mockMvc.perform(post("/api/accounts/{id}/transfer", aliceAccountId)
                        .header("Authorization", "Bearer " + aliceToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"destinationAccountId\":" + aliceAccountId + ",\"amount\":10}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("SAME_ACCOUNT_TRANSFER"));

        mockMvc.perform(get("/api/accounts/{id}", aliceAccountId)
                        .header("Authorization", "Bearer " + bobToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCOUNT_ACCESS_DENIED"));

        mockMvc.perform(get("/api/accounts/{id}/transactions", aliceAccountId)
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(2)))
                .andExpect(jsonPath("$[?(@.type=='DEPOSIT')]").exists())
                .andExpect(jsonPath("$[?(@.type=='TRANSFER_OUT')]").exists());

        mockMvc.perform(get("/api/accounts/{id}/transactions", bobAccountId)
                        .header("Authorization", "Bearer " + bobToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("TRANSFER_IN"))
                .andExpect(jsonPath("$[0].relatedAccountId").value(aliceAccountId));

        mockMvc.perform(post("/api/accounts/{id}/close", bobAccountId)
                        .header("Authorization", "Bearer " + bobToken))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("ACCOUNT_HAS_BALANCE"));

        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
    }

    @Test
    void registeringTheSameEmailTwiceIsRejected() throws Exception {
        registerAndGetToken("duplicate@example.com");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"duplicate@example.com\",\"password\":\"supersecret123\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EMAIL_ALREADY_EXISTS"));
    }

    @Test
    void loginWithWrongPasswordIsRejected() throws Exception {
        registerAndGetToken("wrongpass@example.com");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"wrongpass@example.com\",\"password\":\"incorrect-password\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }

    private String registerAndGetToken(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"supersecret123\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        return jsonNode(result).get("token").asText();
    }

    private Long createAccount(String token, String type) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/accounts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"" + type + "\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        return jsonNode(result).get("id").asLong();
    }

    private void deposit(String token, Long accountId, String amount) throws Exception {
        mockMvc.perform(post("/api/accounts/{id}/deposit", accountId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":" + amount + "}"))
                .andExpect(status().isOk());
    }

    private JsonNode jsonNode(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }
}
