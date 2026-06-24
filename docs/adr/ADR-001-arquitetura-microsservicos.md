# ADR-001: Arquitetura de Microsserviços

**Data:** 2026-06-24  
**Status:** Aceito  
**Autores:** Time de Arquitetura  

---

## Contexto

O sistema precisa suportar 10.000 entregadores simultâneos enviando GPS, clientes rastreando em tempo real e notificações de status. Uma arquitetura monolítica criaria acoplamento entre funcionalidades com requisitos de escala completamente distintos: rastreamento GPS exige muito mais throughput do que autenticação.

## Decisão

Adotamos **6 microsserviços independentes** com um API Gateway centralizado:

| Serviço | Porta | Responsabilidade |
|---------|-------|-----------------|
| `api-gateway` | 8080 | Roteamento, autenticação JWT, rate limiting |
| `auth-service` | 8081 | Login, registro, JWT, Refresh Token, RBAC |
| `delivery-service` | 8082 | CRUD de pedidos, máquina de estados |
| `tracking-service` | 8083 | Recebe GPS, atualiza Redis, produz Kafka |
| `websocket-service` | 8084 | Consome Kafka, faz push STOMP para clientes |
| `notification-service` | 8085 | Consome Kafka, envia FCM/email |

Cada serviço possui seu **próprio banco de dados** (isolamento total de dados).

## Consequências

**Positivas:**
- Escala independente: `websocket-service` pode ter 10 réplicas enquanto `auth-service` tem 2
- Falhas isoladas: queda do `notification-service` não afeta o rastreamento
- Deploy independente por serviço

**Negativas:**
- Complexidade operacional maior (mitigada com Docker Compose → Kubernetes gradual)
- Latência de rede entre serviços (~1-5ms por hop)
- Necessidade de observabilidade distribuída (OpenTelemetry resolve isso)

## Alternativas Consideradas

- **Monolito modular:** descartado — não permite escala independente do módulo de tracking
- **2 serviços (auth + app):** descartado — acoplaria WebSocket e tracking com alto risco de cascata de falhas

## Referências

- [ADR-003](ADR-003-kafka-eventos.md) · [ADR-006](ADR-006-observabilidade.md)
