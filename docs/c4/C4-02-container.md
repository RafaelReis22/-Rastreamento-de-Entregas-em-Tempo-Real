# C4 — Nível 2: Diagrama de Containers

> Mostra os containers (aplicações e bancos de dados) que compõem o sistema.

## Diagrama PlantUML

```plantuml
@startuml C4-Container-Rastreamento
!include https://raw.githubusercontent.com/plantuml-stdlib/C4-PlantUML/master/C4_Container.puml

LAYOUT_WITH_LEGEND()

title Sistema de Rastreamento de Entregas — Diagrama de Containers

Person(entregador, "Entregador", "Envia GPS via app/web")
Person(cliente, "Cliente", "Acompanha entrega no mapa")
Person(admin, "Administrador", "Monitora o sistema")

System_Boundary(rastreamento, "Sistema de Rastreamento") {

    Container(frontend, "Frontend Web", "Next.js 14 / TypeScript", "Interface do entregador e do cliente.\nMapa ao vivo com Leaflet.js.\nWebSocket via SockJS/STOMP.")

    Container(gateway, "API Gateway", "Spring Cloud Gateway", "Ponto de entrada único.\nRoteamento, filtro JWT RS256, rate limiting por IP/token.")

    Container(auth, "Auth Service", "Spring Boot 3 / Java 21", "Autenticação, JWT RS256, Refresh Token,\nRBAC (ADMIN/DELIVERER/CUSTOMER), auditoria.")

    Container(delivery, "Delivery Service", "Spring Boot 3 / Java 21", "CRUD de pedidos.\nMáquina de estados de entrega.\nOpenAPI documentado.")

    Container(tracking, "Tracking Service", "Spring Boot 3 / Java 21", "Recebe POST /v1/location.\nValida GPS (velocidade/salto).\nAtualiza Redis e produz Kafka.")

    Container(websocket, "WebSocket Service", "Spring Boot 3 / Java 21", "Consome Kafka location.updated.\nFaz push STOMP para /topic/order/{id}.\nSockJS como fallback.")

    Container(notification, "Notification Service", "Spring Boot 3 / Java 21", "Consome Kafka delivery.status.changed.\nEnvia push FCM e e-mail SendGrid.")

    ContainerDb(pg_auth, "auth_db", "PostgreSQL 16", "Usuários, refresh tokens, audit_log.")
    ContainerDb(pg_delivery, "delivery_db", "PostgreSQL 16", "Pedidos e histórico de status.")
    ContainerDb(pg_tracking, "tracking_db", "PostgreSQL 16", "Localização atual + location_history particionada.")
    ContainerDb(redis, "Redis 7", "Redis", "Cache loc:{orderID}, blacklist JWT, rate limit counters.")
    ContainerDb(kafka, "Apache Kafka", "Kafka + ZooKeeper", "Topics: location.updated (12p), delivery.status.changed (6p), notification.send (6p).")

    Container(otel, "OTel Collector", "OpenTelemetry", "Recebe traces/métricas/logs de todos os serviços.")
    Container(prometheus, "Prometheus", "Prometheus", "Scrape de métricas /actuator/prometheus.")
    Container(grafana, "Grafana + Loki", "Grafana Stack", "Dashboards, alertas, logs correlacionados.")
}

System_Ext(fcm, "Firebase Cloud Messaging", "Notificações push")
System_Ext(sendgrid, "SendGrid", "E-mail transacional")

Rel(entregador, frontend, "Usa", "HTTPS")
Rel(cliente, frontend, "Usa", "HTTPS / WSS")
Rel(admin, grafana, "Monitora", "HTTPS")

Rel(frontend, gateway, "Requisições API e WebSocket", "HTTPS / WSS")
Rel(gateway, auth, "Roteia /auth/**", "HTTP")
Rel(gateway, delivery, "Roteia /v1/orders/**", "HTTP")
Rel(gateway, tracking, "Roteia /v1/location/**", "HTTP")
Rel(gateway, websocket, "Roteia /ws/**", "WSS")

Rel(auth, pg_auth, "Lê/escreve", "JDBC")
Rel(auth, redis, "Blacklist tokens, rate limit", "Redis Protocol")
Rel(delivery, pg_delivery, "Lê/escreve", "JDBC")
Rel(delivery, kafka, "Produz delivery.status.changed", "Kafka Protocol")
Rel(tracking, pg_tracking, "Lê/escreve", "JDBC")
Rel(tracking, redis, "Cache loc:{orderID}", "Redis Protocol")
Rel(tracking, kafka, "Produz location.updated", "Kafka Protocol")
Rel(websocket, kafka, "Consome location.updated", "Kafka Protocol")
Rel(notification, kafka, "Consome delivery.status.changed", "Kafka Protocol")
Rel(notification, fcm, "Envia push", "HTTPS")
Rel(notification, sendgrid, "Envia e-mail", "HTTPS")

Rel(auth, otel, "Traces + métricas + logs", "OTLP HTTP")
Rel(delivery, otel, "Traces + métricas + logs", "OTLP HTTP")
Rel(tracking, otel, "Traces + métricas + logs", "OTLP HTTP")
Rel(websocket, otel, "Traces + métricas + logs", "OTLP HTTP")
Rel(otel, prometheus, "Exporta métricas", "Prometheus Scrape")
Rel(prometheus, grafana, "Fonte de métricas", "PromQL")
Rel(otel, grafana, "Exporta logs", "Loki Push")

@enduml
```

## Descrição Narrativa

### Fluxo principal de atualização de localização:
1. **Entregador** → `POST /v1/location` → **Frontend** → **API Gateway**
2. Gateway valida JWT e roteia para **Tracking Service**
3. Tracking Service: persiste em `pg_tracking`, atualiza cache `loc:{orderID}` no **Redis**, publica em Kafka `location.updated`
4. **WebSocket Service** consome o evento Kafka e faz push STOMP para `/topic/order/{orderID}`
5. **Cliente** recebe a nova posição no mapa em tempo real

### Separação de dados:
Cada serviço Java possui seu próprio banco PostgreSQL — zero acoplamento direto entre serviços via banco.
