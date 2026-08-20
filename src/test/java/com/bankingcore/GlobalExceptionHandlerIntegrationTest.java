package com.bankingcore;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Regression test: Spring throws NoResourceFoundException (not a 404 directly)
 * for any request path that matches no controller and no static resource. Left
 * unhandled, it used to fall through to GlobalExceptionHandler's catch-all
 * Exception handler and come back as a misleading 500 "Unexpected server
 * error" for every unknown route - including /actuator/health before the
 * actuator dependency existed.
 */
@SpringBootTest
@AutoConfigureMockMvc
class GlobalExceptionHandlerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void unknownRouteUnderAPermitAllPatternReturnsNotFoundNotInternalError() throws Exception {
        mockMvc.perform(get("/api/auth/this-route-does-not-exist"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void actuatorHealthIsUpAndPubliclyReachable() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"status\":\"UP\"}"));
    }

    @Test
    void beanValidationFailureReturnsBadRequestNotInternalError() throws Exception {
        // password too short (@Size(min = 8)) - triggers MethodArgumentNotValidException.
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"valid@example.com\",\"password\":\"short\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("password")));
    }
}
