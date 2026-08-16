package com.bankingcore.auth.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.bankingcore.auth.application.AuthResult;
import com.bankingcore.auth.application.LoginUserUseCase;
import com.bankingcore.auth.application.RegisterUserUseCase;
import com.bankingcore.auth.web.dtos.AuthResponse;
import com.bankingcore.auth.web.dtos.LoginRequest;
import com.bankingcore.auth.web.dtos.RegisterRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUserUseCase loginUserUseCase;

    public AuthController(RegisterUserUseCase registerUserUseCase, LoginUserUseCase loginUserUseCase) {
        this.registerUserUseCase = registerUserUseCase;
        this.loginUserUseCase = loginUserUseCase;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return toResponse(registerUserUseCase.execute(request.email(), request.password()));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return toResponse(loginUserUseCase.execute(request.email(), request.password()));
    }

    private AuthResponse toResponse(AuthResult result) {
        return new AuthResponse(result.userId(), result.email(), result.token());
    }
}
