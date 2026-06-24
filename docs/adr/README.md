# Architecture Decision Records (ADRs)

Registro das decisões arquiteturais do projeto **Rastreamento de Entregas em Tempo Real**.

## Índice

| ADR | Título | Status |
|-----|--------|--------|
| [ADR-001](ADR-001-arquitetura-microsservicos.md) | Arquitetura de Microsserviços | Aceito |
| [ADR-002](ADR-002-postgresql-redis.md) | PostgreSQL para persistência e Redis como cache | Aceito |
| [ADR-003](ADR-003-kafka-eventos.md) | Apache Kafka para eventos de localização | Aceito |
| [ADR-004](ADR-004-websocket-stomp-sockjs.md) | WebSocket com STOMP sobre SockJS | Aceito |
| [ADR-005](ADR-005-jwt-refresh-token.md) | JWT RS256, Refresh Token e RBAC | Aceito |
| [ADR-006](ADR-006-observabilidade.md) | Observabilidade com OpenTelemetry | Aceito |
| [ADR-007](ADR-007-deploy-infraestrutura.md) | Deploy Blue-Green e Infraestrutura | Aceito |

## Como criar um novo ADR

1. Copie o template abaixo
2. Numeração sequencial: `ADR-NNN-titulo-kebab-case.md`
3. Status possíveis: `Proposto` → `Aceito` → `Depreciado` / `Substituído por ADR-NNN`

```markdown
# ADR-NNN: Título

**Data:** YYYY-MM-DD
**Status:** Proposto

## Contexto
## Decisão
## Consequências
## Alternativas Consideradas
## Referências
```
