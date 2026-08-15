package com.bankingcore.backend.auth.web.dtos;

public record AuthResponse(Long userId, String email, String token) {
}
