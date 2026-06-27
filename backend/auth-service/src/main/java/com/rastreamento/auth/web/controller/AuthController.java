package com.rastreamento.auth.web.controller;

import com.rastreamento.auth.application.dto.LoginRequest;
import com.rastreamento.auth.application.dto.RefreshRequest;
import com.rastreamento.auth.application.dto.RegisterRequest;
import com.rastreamento.auth.application.dto.TokenResponse;
import com.rastreamento.auth.application.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Autenticação e gerenciamento de tokens JWT")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar novo usuário", description = "Cria conta e retorna par de tokens")
    public TokenResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Autenticar usuário", description = "Valida credenciais e retorna par de tokens")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renovar access token", description = "Troca refresh token por novo par de tokens")
    public TokenResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return authService.refresh(request);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Encerrar sessão", description = "Invalida access token e revoga todos os refresh tokens do usuário")
    public void logout(@RequestHeader("Authorization") String authHeader,
                       Authentication authentication) {
        String token = authHeader.substring(7);
        UUID userId = UUID.fromString(authentication.getName());
        authService.logout(token, userId);
    }
}
