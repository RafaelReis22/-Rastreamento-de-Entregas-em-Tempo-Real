# ADR-007: Estratégia de Deploy e Infraestrutura

**Data:** 2026-06-24  
**Status:** Aceito  

---

## Contexto

O projeto precisa de uma estratégia de deploy que permita:
- Desenvolvimento local simples e rápido
- Deploy de MVP sem overhead operacional de Kubernetes
- Caminho claro de evolução para produção escalável
- Rollback em < 5 minutos (RTO ≤ 15min, RPO ≤ 5min)

## Decisão

**Estratégia em três estágios:**

### Estágio 1 — Local / Desenvolvimento
**Docker Compose** com todos os serviços de infraestrutura:
```bash
docker compose up -d  # sobe tudo: PG, Redis, Kafka, Grafana, etc.
```
Serviços Java rodam diretamente (`mvn spring-boot:run`) para hot reload.

### Estágio 2 — MVP / Staging + Produção Inicial
**Docker + VPS** (DigitalOcean / AWS EC2):
- Cada microsserviço como container Docker independente
- Nginx como reverse proxy com SSL (Let's Encrypt via certbot)
- Scripts de deploy automatizados em `infra/scripts/deploy.sh`
- **Frontend:** Vercel (deploy automático via Git push)

### Estágio 3 — Pós-MVP / Produção Escalável
**Kubernetes** com Helm charts:
- HorizontalPodAutoscaler para `tracking-service` e `websocket-service`
- Strimzi Operator para Kafka gerenciado no cluster
- cert-manager para TLS automático
- Ingress NGINX com sticky session para WebSocket

**Estratégia de deploy sem downtime — Blue-Green:**
```
[Load Balancer]
      |
   [Blue] ← tráfego atual (v1.0)
   [Green] ← nova versão (v1.1) recebe 0% até aprovação
      |
  smoke tests passam → swap de tráfego → Blue destruído
```

**Rollback automático:**
- Health check falha em 2min pós-deploy → tráfego volta para Blue
- Script: `infra/scripts/rollback.sh <versão>`
- Taxa de erro 5xx > 1% → alerta Grafana → rollback manual ou automático

**Rotação de chaves JWT:**
- A cada 30 dias via script automatizado
- JWKS endpoint suporta múltiplas chaves ativas durante transição
- Zero downtime para clientes com tokens ativos

## Consequências

**Positivas:**
- Docker Compose mantém paridade local/produção
- Kubernetes é opcional — não bloqueia MVP
- Blue-Green garante rollback < 5 minutos
- Vercel simplifica deploy e escala do frontend automaticamente

**Negativas:**
- Dois ambientes (Docker direto + Kubernetes futuro) requerem dois conjuntos de configuração
- VPS único é SPOF para MVP (mitigado com backups e PITR do PostgreSQL)

## Alternativas Consideradas

- **Kubernetes desde o início:** descartado — overhead operacional desnecessário antes de validar demanda
- **Heroku/Railway:** descartado — custo elevado, menor controle sobre rede e dados LGPD
- **Canary deploy:** considerado para pós-MVP; Blue-Green é mais simples para equipe pequena

## Referências

- [ADR-001](ADR-001-arquitetura-microsservicos.md) — Serviços a deployar
- [ADR-005](ADR-005-jwt-refresh-token.md) — Rotação de chaves JWT no deploy
- [ADR-006](ADR-006-observabilidade.md) — Monitoramento pós-deploy
