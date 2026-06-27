package com.rastreamento.auth.infrastructure.security;

import com.rastreamento.auth.domain.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class JwtService {

    private final KeyPair keyPair;
    private final String keyId;
    private final long accessTokenExpirationMs;

    public JwtService(
        KeyPair keyPair,
        JwtProperties props,
        @Value("${jwt.access-token-expiration-ms:3600000}") long accessTokenExpirationMs
    ) {
        this.keyPair = keyPair;
        this.keyId = props.getKeyId();
        this.accessTokenExpirationMs = accessTokenExpirationMs;
    }

    public String generateAccessToken(User user) {
        Date now = new Date();
        return Jwts.builder()
            .id(UUID.randomUUID().toString())
            .header().add("kid", keyId).and()
            .subject(user.getId().toString())
            .claim("email", user.getEmail())
            .claim("role", user.getRole().name())
            .issuedAt(now)
            .expiration(new Date(now.getTime() + accessTokenExpirationMs))
            .signWith(keyPair.getPrivate(), Jwts.SIG.RS256)
            .compact();
    }

    public Claims parseAccessToken(String token) {
        return Jwts.parser()
            .verifyWith((java.security.PublicKey) keyPair.getPublic())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    public Map<String, Object> getJwks() {
        RSAPublicKey pub = (RSAPublicKey) keyPair.getPublic();
        Map<String, Object> jwk = Map.of(
            "kty", "RSA",
            "use", "sig",
            "alg", "RS256",
            "kid", keyId,
            "n", toBase64Url(pub.getModulus()),
            "e", toBase64Url(pub.getPublicExponent())
        );
        return Map.of("keys", List.of(jwk));
    }

    private String toBase64Url(BigInteger value) {
        byte[] bytes = value.toByteArray();
        int offset = (bytes[0] == 0) ? 1 : 0;
        return Base64.getUrlEncoder().withoutPadding()
            .encodeToString(Arrays.copyOfRange(bytes, offset, bytes.length));
    }
}
