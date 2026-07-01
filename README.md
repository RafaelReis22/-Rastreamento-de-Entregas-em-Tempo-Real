# Rastreamento de Entregas em Tempo Real

Plataforma enterprise de rastreamento logístico em tempo real com arquitetura de microsserviços orientada a eventos (Event-Driven Architecture), ingestão de telemetria GPS de alta vazão, indexação geoespacial no Redis Geo, push via WebSockets STOMP e visualização interativa em mapa Leaflet/OpenStreetMap.

---

## 📐 Arquitetura do Sistema

```mermaid
graph TD
    Client[📱 App Entregador / Cliente] -->|HTTP / REST| Gateway[🌐 API Gateway :8080]
    Gateway -->|Auth & Token| AuthSvc[🔐 auth-service :8081]
    Gateway -->|Gestão de Pedidos| DeliverySvc[📦 delivery-service :8082]
    Gateway -->|Pings GPS| TrackingSvc[📍 tracking-service :8083]
    
    TrackingSvc -->|GeoIndex GEOADD| Redis[(⚡ Redis Cache & Geo)]
    TrackingSvc -->|Publish location.updated| Kafka{{🚀 Apache Kafka Broker}}
    DeliverySvc -->|Publish delivery.status.changed| Kafka
    
    Kafka -->|Consume location.updated| WSSvc[🔌 websocket-service :8084]
    Kafka -->|Consume notification.send| NotifSvc[🔔 notification-service :8085]
    
    WSSvc -->|Push STOMP /topic/tracking| WebApp[💻 Frontend Web SPA :3000]
    
    AuthSvc --> DBAuth[(🗄️ PostgreSQL auth_db :5432)]
    DeliverySvc --> DBDelivery[(🗄️ PostgreSQL delivery_db :5433)]
    TrackingSvc --> DBTracking[(🗄️ PostgreSQL tracking_db :5434)]
```

---

## 🛠️ Stack Tecnológica

| Camada | Tecnologia |
|--------|------------|
| **Linguagem Backend** | Java 21 (OpenJDK / Temurin) |
| **Framework Backend** | Spring Boot 3.3 / Spring Cloud Gateway |
| **Frontend Web** | Next.js 14, React 18, TypeScript, Leaflet + OpenStreetMap |
| **Event Streaming** | Apache Kafka 7.6 + ZooKeeper (12 partições para GPS) |
| **Banco de Dados** | PostgreSQL 16 (3 instâncias isoladas: `auth_db`, `delivery_db`, `tracking_db`) |
| **Cache & Geo Indexing** | Redis 7 (`GEOADD`, `GEORADIUS`, Token Blacklist) |
| **WebSockets** | STOMP sobre WebSocket (Spring Messaging) |
| **Observabilidade** | OpenTelemetry Collector, Prometheus 2.51, Grafana 10.4, Loki, Promtail |
| **DevOps & Containers** | Docker Compose v2 |

---

## 🚀 Serviços e Portas

| Serviço | Porta | Descrição |
|---------|-------|-----------|
| `api-gateway` | 8080 | Gateway de Roteamento Spring Cloud Gateway |
| `auth-service` | 8081 | Autenticação IAM, JWT RS256 e Refresh Token |
| `delivery-service` | 8082 | Gestão de entregas, pacotes e status de pedidos |
| `tracking-service` | 8083 | Ingestão de coordenadas GPS e Redis Geo |
| `websocket-service` | 8084 | Servidor WebSocket STOMP para push de localização ao vivo |
| `notification-service` | 8085 | Processador de notificações SMS / Email / Push |
| `frontend-web` | 3000 | Interface Web SPA com Mapa Leaflet |
| `PostgreSQL (auth)` | 5432 | Banco relacional de autenticação |
| `PostgreSQL (delivery)` | 5433 | Banco relacional de entregas |
| `PostgreSQL (tracking)` | 5434 | Banco relacional de histórico GPS |
| `Redis` | 6379 | Cache e índice espacial |
| `Kafka` | 9092 / 29092 | Event broker |
| `Prometheus` | 9090 | Métricas de observabilidade |
| `Grafana` | 3000 (infra) | Dashboards visuais |

---

## ⚡ Início Rápido (5 minutos)

### 1. Clonar e subir a infraestrutura Docker

```bash
git clone https://github.com/RafaelReis22/-Rastreamento-de-Entregas-em-Tempo-Real.git
cd -Rastreamento-de-Entregas-em-Tempo-Real

# Subir bancos PostgreSQL, Redis, Kafka, OTel e Grafana
docker compose up -d
```

Aguarde até que os serviços fiquem saudáveis (`docker compose ps`).

### 2. Compilar os Microsserviços Backend

```bash
cd backend
mvn clean package -DskipTests
```

### 3. Executar os Microsserviços

Em terminais separados:
```bash
cd backend/auth-service && java -jar target/*.jar
cd backend/delivery-service && java -jar target/*.jar
cd backend/tracking-service && java -jar target/*.jar
cd backend/websocket-service && java -jar target/*.jar
cd backend/api-gateway && java -jar target/*.jar
```

### 4. Executar o Frontend Web Next.js

```bash
cd frontend-web
npm install
npm run dev
```

Acesse o painel em: **http://localhost:3000**

### 5. Executar o Simulador de Pings GPS

Para simular um entregador em trânsito pela Av. Paulista e ver o marcador se movendo no mapa:

```bash
node infra/scripts/gps-simulator.js
```

---

## 🛰️ Tópicos Kafka e Eventos

| Tópico Kafka | Partições | Descrição |
|--------------|-----------|-----------|
| `location.updated` | 12 | Pings de telemetria GPS do entregador |
| `delivery.status.changed` | 6 | Mudanças de estado da entrega (`PENDING` -> `IN_TRANSIT` -> `DELIVERED`) |
| `notification.send` | 6 | Eventos disparadores de notificação ao cliente |
| `location.updated.DLT` | 3 | Dead Letter Queue para falhas de processamento GPS |

---

## 🏆 Matriz de Entregas por Fase (Fases 1 a 12 - 100% Concluído)

| Fase | Funcionalidade | Status | Módulos Envolvidos |
|------|----------------|--------|---------------------|
| **Fase 1** | README.md completo, DDLs SQL e Simulador GPS Node.js | ✅ **CONCLUÍDO** | `README.md`, `infra/scripts` |
| **Fase 2** | JWT RS256, Auth Security & `JwtAuthFilter` Gateway | ✅ **CONCLUÍDO** | `api-gateway`, `auth-service` |
| **Fase 3** | Ciclo de Vida de Entregas & Kafka `delivery.status.changed` | ✅ **CONCLUÍDO** | `delivery-service` |
| **Fase 4** | Ingestão GPS, Redis Geo (`GEOADD`) & Kafka `location.updated` | ✅ **CONCLUÍDO** | `tracking-service` |
| **Fase 5** | Servidor WebSocket STOMP & Push ao vivo | ✅ **CONCLUÍDO** | `websocket-service`, `notification-service` |
| **Fase 6** | Frontend Web SPA Next.js 14 com Mapa Leaflet | ✅ **CONCLUÍDO** | `frontend-web` |
| **Fase 7** | OSRM Map Matching & Snapping de Coordenadas à Malha Viária | ✅ **CONCLUÍDO** | `infra/osrm`, `tracking-service` |
| **Fase 8** | Predição Dinâmica de ETA por IA / Modelo de Velocidade Média | ✅ **CONCLUÍDO** | `delivery-service` (`EtaCalculatorService`) |
| **Fase 9** | Cerca Virtual (Geofencing 500m) & Alertas Automáticos | ✅ **CONCLUÍDO** | `tracking-service` (`GeofenceAlertService`) |
| **Fase 10** | Protocolo de Ingestão Offline & Sync em Lote (`/batch`) | ✅ **CONCLUÍDO** | `infra/scripts`, `tracking-service` |
| **Fase 11** | Gestão Multi-Frota (Multi-Tenancy) & Medição FinOps (km / R$) | ✅ **CONCLUÍDO** | `delivery-service` (`FleetMeteringService`) |
| **Fase 12** | Helm Chart Kubernetes, HPA Auto-Scaling & Push Git Final | ✅ **CONCLUÍDO** | `infra/k8s/helm`, `git push` |

---

## 📄 Licença

MIT License — consulte o arquivo `LICENSE` para mais detalhes.