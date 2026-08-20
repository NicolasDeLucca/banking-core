package com.bankingcore.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import com.bankingcore.shared.web.CorrelationIdFilter;

@Configuration
public class WebConfig {

    /**
     * HIGHEST_PRECEDENCE so this runs before Spring Security's own filter
     * chain (registered by Boot around order -100) - the request id needs to
     * exist for the *whole* request, including an auth rejection that never
     * reaches a controller.
     */
    @Bean
    FilterRegistrationBean<CorrelationIdFilter> correlationIdFilter() {
        FilterRegistrationBean<CorrelationIdFilter> registration = new FilterRegistrationBean<>(new CorrelationIdFilter());
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }
}
