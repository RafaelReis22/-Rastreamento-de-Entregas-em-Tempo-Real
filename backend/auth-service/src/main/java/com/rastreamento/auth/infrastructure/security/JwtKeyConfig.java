package com.rastreamento.auth.infrastructure.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Slf4j
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtKeyConfig {

    @Bean
    public KeyPair jwtKeyPair(JwtProperties props) throws Exception {
        if (props.getPrivateKey().isBlank() || props.getPublicKey().isBlank()) {
            log.warn("=== JWT keys não configuradas — gerando par RSA efêmero. NÃO USE EM PRODUÇÃO. ===");
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(2048);
            return gen.generateKeyPair();
        }
        KeyFactory kf = KeyFactory.getInstance("RSA");
        byte[] privBytes = Base64.getDecoder().decode(props.getPrivateKey().replaceAll("\\s", ""));
        byte[] pubBytes  = Base64.getDecoder().decode(props.getPublicKey().replaceAll("\\s", ""));
        return new KeyPair(
            kf.generatePublic(new X509EncodedKeySpec(pubBytes)),
            kf.generatePrivate(new PKCS8EncodedKeySpec(privBytes))
        );
    }
}
