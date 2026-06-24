# C4 — Nível 3: Diagrama de Componentes — Tracking Service

> Detalha os componentes internos do Tracking Service, que é o coração do fluxo de localização.

## Diagrama PlantUML

```plantuml
@startuml C4-Component-TrackingService
!include https://raw.githubusercontent.com/plantuml-stdlib/C4-PlantUML/master/C4_Component.puml

LAYOUT_WITH_LEGEND()

title Tracking Service — Diagrama de Componentes

Container_Ext(gateway, "API Gateway", "Spring Cloud Gateway", "Valida JWT e roteia requisições")
ContainerDb_Ext(pg_tracking, "tracking_db", "PostgreSQL 16", "Persistência de pedidos e histórico")
ContainerDb_Ext(redis, "Redis 7", "Redis", "Cache de localização")
ContainerDb_Ext(kafka, "Apache Kafka", "Kafka", "Topic location.updated")
Container_Ext(otel, "OTel Collector", "OpenTelemetry", "Coleta traces e métricas")

Container_Boundary(tracking, "Tracking Service :8083") {

    Component(controller, "LocationController", "Spring MVC @RestController", "Recebe POST /v1/location e GET /v1/orders/{id}/location.\nAnotado com @Operation (OpenAPI).")

    Component(validator, "LocationValidator", "Spring @Service", "Valida coordenadas: latitude [-90,90], longitude [-180,180].\nDetecta velocidade impossível (> 200 km/h).\nDetecta salto geográfico impossível (> 500m em < 1s).\nFlag is_suspicious=true se inválido.")

    Component(location_service, "LocationService", "Spring @Service", "Orquestra o fluxo:\n1. Valida via LocationValidator\n2. Persiste no banco\n3. Atualiza cache Redis\n4. Publica evento Kafka")

    Component(order_repo, "DeliveryOrderRepository", "Spring Data JPA @Repository", "findById, updateLastLocation.\nQuery: UPDATE delivery_orders SET last_lat, last_lng, updated_at WHERE id = :orderId")

    Component(history_repo, "LocationHistoryRepository", "Spring Data JPA @Repository", "save(LocationHistory).\nfindByOrderIdAndRecordedAtBetween (histórico paginado).")

    Component(redis_cache, "RedisLocationCache", "Spring @Component", "SET loc:{orderID} (Hash) com TTL 1h.\nGET com fallback para banco se miss.")

    Component(kafka_producer, "KafkaLocationProducer", "Spring @Component", "kafkaTemplate.send('location.updated', orderID, LocationEvent).\nkey=orderID garante ordenação por pedido.\nacks=all, enable.idempotence=true.")

    Component(otel_instrumentation, "OtelInstrumentation", "Micrometer + OTel", "Incrementa counter location_updates_total.\nRegistra span @WithSpan em métodos críticos.\nExporta para OTel Collector.")
}

Rel(gateway, controller, "POST /v1/location\nGET /v1/orders/{id}/location", "HTTP")
Rel(controller, location_service, "locationService.processUpdate(dto)")
Rel(controller, otel_instrumentation, "Métricas por requisição")
Rel(location_service, validator, "validator.validate(dto)")
Rel(location_service, order_repo, "updateLastLocation(orderId, lat, lng)")
Rel(location_service, history_repo, "save(locationHistory)")
Rel(location_service, redis_cache, "cache.set(orderId, lat, lng)")
Rel(location_service, kafka_producer, "producer.publish(locationEvent)")
Rel(location_service, otel_instrumentation, "Span do fluxo completo")
Rel(order_repo, pg_tracking, "JDBC / Hibernate", "SQL")
Rel(history_repo, pg_tracking, "JDBC / Hibernate", "SQL")
Rel(redis_cache, redis, "SET / GET / EXPIRE", "Redis Protocol")
Rel(kafka_producer, kafka, "Produz LocationEvent", "Kafka Protocol")
Rel(otel_instrumentation, otel, "Traces + métricas", "OTLP HTTP")

@enduml
```

## Responsabilidade de Cada Componente

| Componente | Padrão | Responsabilidade |
|-----------|--------|-----------------|
| `LocationController` | REST Controller | Entrada/saída HTTP, validação de request body |
| `LocationValidator` | Validator | Regras de negócio de GPS (velocidade, salto, coordenadas) |
| `LocationService` | Application Service | Orquestração do fluxo completo |
| `DeliveryOrderRepository` | Repository | Atualiza snapshot de localização no pedido |
| `LocationHistoryRepository` | Repository | Persiste histórico particionado |
| `RedisLocationCache` | Cache Adapter | Leitura rápida < 5ms, fallback automático para DB |
| `KafkaLocationProducer` | Event Publisher | Publicação confiável com `acks=all` |
| `OtelInstrumentation` | Cross-cutting | Métricas e traces distribuídos |

## Fluxo de Dados: POST /v1/location

```
Cliente → [LocationController]
            → [LocationValidator] ──(inválido)──→ 400 Bad Request
            → [LocationService]
                → [DeliveryOrderRepository] → pg_tracking (UPDATE snapshot)
                → [LocationHistoryRepository] → pg_tracking (INSERT particionado)
                → [RedisLocationCache] → Redis (SET loc:{orderId} TTL 1h)
                → [KafkaLocationProducer] → Kafka topic location.updated
            → 200 OK
```
