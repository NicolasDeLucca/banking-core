package com.bankingcore.backend.auth.infrastructure.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.bankingcore.backend.auth.application.TokenClaims;
import com.bankingcore.backend.auth.application.TokenProvider;
import com.bankingcore.backend.shared.error.AuthenticationFailedException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Reads the Bearer token from every request, validates it via TokenProvider
 * and populates the SecurityContext. Without this filter, "anyRequest().authenticated()"
 * in SecurityConfig has no way of ever succeeding.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final TokenProvider tokenProvider;

    public JwtAuthenticationFilter(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith(BEARER_PREFIX) && SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = header.substring(BEARER_PREFIX.length());
            try {
                authenticate(token);
            } catch (AuthenticationFailedException exception) {
                // Leave the SecurityContext unauthenticated: downstream authorization
                // rules will reject the request via the configured entry point.
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    private void authenticate(String token) {
        TokenClaims claims = tokenProvider.parseToken(token);

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + claims.role().name()));

        var authentication = new UsernamePasswordAuthenticationToken(claims.userId(), null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
