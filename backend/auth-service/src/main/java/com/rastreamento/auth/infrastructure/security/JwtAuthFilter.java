package com.rastreamento.auth.infrastructure.security;

import com.rastreamento.auth.infrastructure.redis.TokenBlacklistService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final TokenBlacklistService blacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = authHeader.substring(7);
        try {
            Claims claims = jwtService.parseAccessToken(token);
            String jti = claims.getId();
            if (jti != null && blacklistService.isBlacklisted(jti)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            String role = claims.get("role", String.class);
            var auth = new UsernamePasswordAuthenticationToken(
                claims.getSubject(), null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (JwtException ignored) {
            // token inválido — continua sem autenticação; endpoints protegidos retornarão 401
        }
        filterChain.doFilter(request, response);
    }
}
