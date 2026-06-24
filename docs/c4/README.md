# Diagramas C4

Diagramas de arquitetura do sistema **Rastreamento de Entregas em Tempo Real** seguindo o modelo C4.

## Nível 1 — Contexto

[C4-01-context.md](C4-01-context.md)

Mostra o sistema como uma caixa preta e seus usuários externos (Entregador, Cliente, Administrador) e integrações externas (FCM, SendGrid, Leaflet/OSM).

## Nível 2 — Containers

[C4-02-container.md](C4-02-container.md)

Detalha os 6 microsserviços, 3 bancos PostgreSQL, Redis, Kafka, frontend Next.js e stack de observabilidade. Inclui o fluxo de dados da localização em tempo real.

## Nível 3 — Componentes

[C4-03-component-tracking.md](C4-03-component-tracking.md)

Zoom no **Tracking Service** — componente central do sistema. Mostra Controller, Validator, Service, Repositories, Redis Cache, Kafka Producer e OTel Instrumentation.

## Como visualizar os diagramas PlantUML

- **VS Code:** extensão [PlantUML](https://marketplace.visualstudio.com/items?itemName=jebbs.plantuml)
- **Online:** [plantuml.com/plantuml](https://www.plantuml.com/plantuml/uml/)
- **IntelliJ IDEA:** plugin PlantUML Integration
