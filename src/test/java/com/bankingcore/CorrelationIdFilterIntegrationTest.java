package com.bankingcore;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.bankingcore.shared.web.CorrelationIdFilter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
class CorrelationIdFilterIntegrationTest {

    private static final String UUID_PATTERN =
            "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void everyResponseCarriesAUniqueRequestId() throws Exception {
        MvcResult first = mockMvc.perform(get("/actuator/health")).andReturn();
        MvcResult second = mockMvc.perform(get("/actuator/health")).andReturn();

        String firstId = first.getResponse().getHeader(CorrelationIdFilter.REQUEST_ID_HEADER);
        String secondId = second.getResponse().getHeader(CorrelationIdFilter.REQUEST_ID_HEADER);

        assertThat(firstId).matches(UUID_PATTERN);
        assertThat(secondId).matches(UUID_PATTERN);
        assertThat(firstId).isNotEqualTo(secondId);
    }

    @Test
    void aRequestRejectedByAuthenticationStillCarriesARequestId() throws Exception {
        // Proves the filter runs ahead of Spring Security's chain, not just
        // ahead of controller code - this request never reaches a controller.
        MvcResult result = mockMvc.perform(get("/api/accounts")).andReturn();

        assertThat(result.getResponse().getHeader(CorrelationIdFilter.REQUEST_ID_HEADER)).matches(UUID_PATTERN);
    }
}
