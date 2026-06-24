# ADR-002: PostgreSQL como persistência e Redis exclusivamente como cache

**Data:** 2026-06-24  
**Status:** Aceito  

---

## Contexto

O sistema precisa armazenar dados críticos de pedidos e histórico de localização (retenção LGPD de 90 dias), além de servir a última posição conhecida do entregador com latência < 50ms para o WebSocket.

## Decisão

**PostgreSQL 16** para toda persistência de dados:
- `auth_db` — usuários, refresh tokens, audit log
- `delivery_db` — pedidos e histórico de transições de status
- `tracking_db` — snapshot de localização atual + `location_history` particionada mensalmente

**Redis 7** exclusivamente para:
- Cache da última localização: `loc:{orderID}` (Hash, TTL 1h)
- Blacklist de tokens JWT revogados: `blacklist:{jti}` (TTL = expiração do token)
- Contadores de rate limiting por IP/token

Redis **NÃO** é usado para Pub/Sub de eventos (substituído pelo Kafka — ver ADR-003).

## Consequências

**Positivas:**
- Cache miss no Redis tem fallback automático para PostgreSQL — sistema resiliente à perda de cache
- Particionamento mensal da `location_history` mantém queries rápidas mesmo com bilhões de registros
- Redis perde dados no restart mas isso é aceitável pois é apenas cache

**Negativas:**
- Dois sistemas de dados para operar (Redis + PostgreSQL)
- PgBouncer necessário para connection pooling em alta escala

## Alternativas Consideradas

- **Redis Pub/Sub para eventos:** descartado — sem durabilidade, sem replay, sem múltiplos consumer groups
- **MongoDB para histórico de localização:** descartado — PostgreSQL com particionamento é superior para queries temporais e LGPD compliance

## Schema Resumido

```sql
-- tracking_db: particionada mensalmente
CREATE TABLE location_history (
    id           BIGSERIAL,
    order_id     UUID NOT NULL,
    lat          DECIMAL(10,8) NOT NULL,
    lng          DECIMAL(11,8) NOT NULL,
    recorded_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (id, recorded_at)
) PARTITION BY RANGE (recorded_at);
```

## Referências

- [ADR-003](ADR-003-kafka-eventos.md) — Kafka substitui Redis Pub/Sub
