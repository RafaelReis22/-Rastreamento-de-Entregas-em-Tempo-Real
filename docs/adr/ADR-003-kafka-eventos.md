# ADR-003: Apache Kafka para eventos de localização

**Data:** 2026-06-24  
**Status:** Aceito  

---

## Contexto

O sistema precisa propagar eventos de localização do `tracking-service` para o `websocket-service` e o `notification-service` de forma confiável. Com 10.000 entregadores enviando atualizações a cada 3 segundos, isso representa ~3.333 mensagens/segundo em pico.

## Decisão

**Apache Kafka** substitui Redis Pub/Sub para todos os eventos do sistema.

| Topic | Partições | Retenção | Chave |
|-------|-----------|----------|-------|
| `location.updated` | 12 | 7 dias | `orderID` |
| `delivery.status.changed` | 6 | 7 dias | `orderID` |
| `notification.send` | 6 | 7 dias | `userID` |
| `*.DLT` (Dead Letter) | 3 | 30 dias | — |

**Configurações do Producer:**
```java
acks=all          // zero perda de mensagens
retries=3
enable.idempotence=true
```

**Configurações do Consumer:**
```java
enable.auto.commit=false  // commit manual após processamento
auto.offset.reset=latest
```

## Consequências

**Positivas:**
- Durabilidade: mensagens persistidas em disco (retenção 7 dias)
- Replay: possível reprocessar eventos desde qualquer offset
- Múltiplos consumers independentes: `websocket-service` e `notification-service` em grupos separados
- Chave `orderID` garante ordenação de eventos por pedido dentro da mesma partição
- Suporte a 100k+ msg/s com 12 partições

**Negativas:**
- Latência adicional de ~20-50ms vs Redis Pub/Sub (aceitável dado SLA < 1s)
- Operação mais complexa: ZooKeeper, brokers, replication factor
- Overhead de serialização JSON

## Alternativas Consideradas

- **Redis Pub/Sub:** descartado — sem durabilidade, sem replay, sem múltiplos consumer groups independentes
- **RabbitMQ:** descartado — throughput inferior ao Kafka para este volume; menos adequado para event streaming

## Referências

- [ADR-001](ADR-001-arquitetura-microsservicos.md) — Arquitetura geral
- [ADR-002](ADR-002-postgresql-redis.md) — Redis apenas como cache
