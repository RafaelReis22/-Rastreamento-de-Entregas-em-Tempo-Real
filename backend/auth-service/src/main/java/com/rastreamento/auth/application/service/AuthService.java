package com.rastreamento.auth.application.service;

import com.rastreamento.auth.application.dto.LoginRequest;
import com.rastreamento.auth.application.dto.RefreshRequest;
import com.rastreamento.auth.application.dto.RegisterRequest;
import com.rastreamento.auth.application.dto.TokenResponse;
import com.rastreamento.auth.domain.entity.RefreshToken;
import com.rastreamento.auth.domain.entity.User;
import com.rastreamento.auth.domain.repository.RefreshTokenRepository;
import com.rastreamento.auth.domain.repository.UserRepository;
import com.rastreamento.auth.infrastructure.redis.TokenBlacklistService;
import com.rastreamento.auth.infrastructure.security.JwtProperties;
import com.rastreamento.auth.infrastructure.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final TokenBlacklistService blacklistService;
    private final PasswordEncoder passwordEncoder;
    private final JwtProperties jwtProperties;

    @Transactional
    public TokenResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email já cadastrado");
        }
        User user = User.builder()
            .email(request.email())
            .passwordHash(passwordEncoder.encode(request.password()))
            .role(request.role())
            .active(true)
            .build();
        userRepository.save(user);
        return issueTokenPair(user);
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new BadCredentialsException("Credenciais inválidas"));
        if (!user.isActive()) {
            throw new BadCredentialsException("Conta desativada");
        }
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Credenciais inválidas");
        }
        return issueTokenPair(user);
    }

    @Transactional
    public TokenResponse refresh(RefreshRequest request) {
        String tokenHash = sha256(request.refreshToken());
        RefreshToken stored = refreshTokenRepository.findByTokenHash(tokenHash)
            .orElseThrow(() -> new JwtException("Refresh token inválido"));
        if (stored.isRevoked() || stored.isExpired()) {
            throw new JwtException("Refresh token expirado ou revogado");
        }
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);
        return issueTokenPair(stored.getUser());
    }

    @Transactional
    public void logout(String accessToken, UUID userId) {
        try {
            Claims claims = jwtService.parseAccessToken(accessToken);
            long remainingMs = claims.getExpiration().getTime() - System.currentTimeMillis();
            if (remainingMs > 0) {
                String jti = claims.getId() != null ? claims.getId() : sha256(accessToken);
                blacklistService.blacklist(jti, Duration.ofMillis(remainingMs));
            }
        } catch (JwtException ignored) {
            // token já inválido; prossegue para revogar refresh tokens
        }
        refreshTokenRepository.revokeAllByUserId(userId);
    }

    private TokenResponse issueTokenPair(User user) {
        String rawRefreshToken = UUID.randomUUID().toString();
        long refreshMs = jwtProperties.getRefreshTokenExpirationMs();
        RefreshToken refreshToken = RefreshToken.builder()
            .user(user)
            .tokenHash(sha256(rawRefreshToken))
            .expiresAt(LocalDateTime.now().plusSeconds(refreshMs / 1000))
            .revoked(false)
            .build();
        refreshTokenRepository.save(refreshToken);
        String accessToken = jwtService.generateAccessToken(user);
        return TokenResponse.of(accessToken, jwtProperties.getAccessTokenExpirationMs() / 1000, rawRefreshToken);
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 não disponível", e);
        }
    }
}
