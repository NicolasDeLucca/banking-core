package com.bankingcore.backend.auth.dtos;

public record AuthResponse(Long userId, String email, String token) {
}