package com.rastreamento.auth.infrastructure.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String privateKey = "";
    private String publicKey = "";
    private String keyId = "dev-key-1";
    private long accessTokenExpirationMs = 3600000;
    private long refreshTokenExpirationMs = 604800000;
}
