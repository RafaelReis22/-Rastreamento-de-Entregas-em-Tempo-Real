# ADR-005: Segurança JWT com Refresh Token e RBAC

**Data:** 2026-06-24  
**Status:** Aceito  

---

## Contexto

O sistema possui três perfis de usuário com permissões distintas (ADMIN, DELIVERER, CUSTOMER). Autenticação stateless é necessária para escalar horizontalmente o API Gateway sem sessões compartilhadas. Tokens de longa duração criam risco de comprometimento.

## Decisão

**JWT com algoritmo RS256 (assimétrico):**
- Chave privada apenas no `auth-service` para assinar tokens
- Chave pública via endpoint JWKS público para validação em todos os serviços
- **Access Token:** validade 1 hora
- **Refresh Token:** validade 7 dias, armazenado com hash SHA-256 no PostgreSQL e Redis

**Payload do Access Token:**
```json
{
  "sub": "user-uuid",
  "roles": ["DELIVERER"],
  "orderId": "order-uuid",
  "jti": "token-uuid",
  "iat": 1719100000,
  "exp": 1719103600
}
```

**RBAC por perfil:**

| Perfil | Permissões |
|--------|-----------|
| `ADMIN` | Todos os endpoints |
| `DELIVERER` | `POST /v1/location`, gerenciar próprios pedidos |
| `CUSTOMER` | Ver tracking do próprio pedido, assinar WebSocket |

**Revogação:** blacklist de `jti` no Redis com TTL igual à expiração do token.

**Rotação de chaves:** a cada 30 dias via JWKS endpoint com suporte a múltiplas chaves ativas simultaneamente.

## Consequências

**Positivas:**
- RS256 permite validação sem chave privada — serviços só precisam da chave pública
- Refresh Token com rotação reduz janela de comprometimento
- Blacklist no Redis permite revogação imediata sem banco de dados na rota crítica
- LGPD: auditoria completa de autenticação na tabela `audit_log`

**Negativas:**
- Blacklist cresce com o tempo (mitigado com TTL automático)
- Rotação de chaves requer período de sobreposição (dois JWKS ativos)

## Alternativas Consideradas

- **JWT HS256 (simétrico):** descartado — todos os serviços precisariam da chave secreta, aumentando superfície de ataque
- **OAuth2/OIDC completo:** descartado para MVP — complexidade desnecessária; pode ser adicionado em evolução futura
- **Sessões stateful:** descartado — inviável com escala horizontal

## Referências

- [ADR-001](ADR-001-arquitetura-microsservicos.md) — API Gateway valida JWT
- [ADR-007](ADR-007-deploy-infraestrutura.md) — Rotação de chaves no deploy
