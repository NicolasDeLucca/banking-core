package com.bankingcore.shared.web;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Tags every request with a unique id, before anything else runs (see
 * config.WebConfig - registered at Ordered.HIGHEST_PRECEDENCE, ahead of
 * Spring Security's own filter chain), so every log line for that request -
 * including a JWT rejection or the 500 GlobalExceptionHandler logs - can be
 * tied together by grepping one id, and a caller reporting a problem can
 * hand back the id from the X-Request-Id response header instead of a
 * timestamp and a guess.
 *
 * MDC is thread-local; Tomcat reuses worker threads across requests, so the
 * finally block clearing it isn't optional - without it, a later request on
 * the same thread would inherit a stale id in its own log lines.
 */
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String REQUEST_ID_HEADER = "X-Request-Id";
    public static final String MDC_KEY = "requestId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestId = UUID.randomUUID().toString();
        MDC.put(MDC_KEY, requestId);
        response.setHeader(REQUEST_ID_HEADER, requestId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }
}
