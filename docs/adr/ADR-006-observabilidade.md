# ADR-006: Observabilidade com OpenTelemetry, Prometheus, Grafana e Loki

**Data:** 2026-06-24  
**Status:** Aceito  

---

## Contexto

Com 6 microsserviços independentes, uma requisição de atualização de localização percorre: `api-gateway → tracking-service → kafka → websocket-service`. Sem observabilidade distribuída, diagnosticar latência e falhas é inviável.

## Decisão

**Stack unificada de observabilidade:**

| Ferramenta | Papel |
|-----------|-------|
| **OpenTelemetry SDK** | Instrumentação única em todos os 6 serviços Java |
| **OTel Collector** | Recebe spans/métricas/logs e exporta para os backends |
| **Prometheus** | Coleta métricas via scrape do `/actuator/prometheus` |
| **Grafana** | Dashboards unificados (métricas + logs correlacionados) |
| **Loki + Promtail** | Agregação de logs JSON estruturados com `trace_id` |

**Traces distribuídos:** um único `trace_id` propaga por todos os serviços via HTTP headers e Kafka headers, permitindo rastrear `POST /v1/location` do gateway ao push WebSocket.

**Métricas customizadas obrigatórias:**
- `location_updates_total` — counter por entregador/pedido
- `websocket_connections_active` — gauge de conexões abertas
- `kafka_consumer_lag` — lag por topic/partition/consumer group
- `redis_cache_hit_ratio` — eficiência do cache de localização

**Alertas críticos (Grafana Alerting):**
- Kafka consumer lag > 10.000 mensagens → CRITICAL
- Latência p95 > 1s → CRITICAL
- Taxa de erros 5xx > 1% → CRITICAL
- WebSocket connections > 9.000 → WARNING

## Consequências

**Positivas:**
- OTel é vendor-neutral — pode migrar backends sem mudar código de aplicação
- `trace_id` correlacionado em todos os logs JSON facilita debugging
- Dashboards pré-configurados via Grafana provisioning (zero configuração manual)

**Negativas:**
- Overhead de ~2-5% CPU por instrumentação (aceitável)
- Volume de dados de trace em alta carga pode ser alto (sampling configurável)

## Alternativas Consideradas

- **Datadog/New Relic:** descartados — custo elevado para MVP; OTel permite migrar para eles depois sem alterar código
- **Jaeger standalone:** descartado — OTel Collector já exporta para múltiplos backends incluindo Jaeger

## Referências

- [ADR-001](ADR-001-arquitetura-microsservicos.md) — Todos os serviços instrumentados
- [ADR-007](ADR-007-deploy-infraestrutura.md) — Stack de observabilidade no docker-compose
