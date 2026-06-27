package com.rastreamento.auth.web.controller;

import com.rastreamento.auth.infrastructure.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Tag(name = "JWKS", description = "Chave pública RSA para validação de tokens por outros serviços")
public class JwksController {

    private final JwtService jwtService;

    @GetMapping(value = "/.well-known/jwks.json", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Obter JSON Web Key Set", description = "Expõe a chave pública RSA em formato JWK para validação de tokens")
    public Map<String, Object> getJwks() {
        return jwtService.getJwks();
    }
}
