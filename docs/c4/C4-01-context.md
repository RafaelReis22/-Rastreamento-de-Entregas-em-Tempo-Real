# C4 — Nível 1: Diagrama de Contexto

> Mostra o sistema e seus usuários externos e sistemas de terceiros.

## Diagrama PlantUML

```plantuml
@startuml C4-Context-Rastreamento
!include https://raw.githubusercontent.com/plantuml-stdlib/C4-PlantUML/master/C4_Context.puml

LAYOUT_WITH_LEGEND()

title Sistema de Rastreamento de Entregas — Diagrama de Contexto

Person(entregador, "Entregador", "Profissional que realiza entregas.\nUsa app/web para enviar localização em tempo real.")
Person(cliente, "Cliente", "Pessoa que fez o pedido.\nAcompanha a entrega pelo mapa em tempo real.")
Person(admin, "Administrador", "Equipe de operações.\nMonitora pedidos, entregadores e métricas do sistema.")

System(rastreamento, "Sistema de Rastreamento", "Coleta localização GPS dos entregadores,\npersiste histórico e notifica clientes em tempo real.")

System_Ext(fcm, "Firebase Cloud Messaging", "Envia notificações push para dispositivos móveis.")
System_Ext(sendgrid, "SendGrid", "Envia notificações por e-mail.")
System_Ext(maps, "Leaflet.js + OSM", "Renderiza o mapa no navegador do cliente.\nSem dependência de Google Maps.")

Rel(entregador, rastreamento, "Envia localização GPS", "HTTPS POST /v1/location")
Rel(cliente, rastreamento, "Acompanha entrega no mapa", "HTTPS + WebSocket/STOMP")
Rel(admin, rastreamento, "Monitora pedidos e métricas", "HTTPS")
Rel(rastreamento, fcm, "Envia notificação push", "HTTPS")
Rel(rastreamento, sendgrid, "Envia e-mail de status", "HTTPS")
Rel(rastreamento, maps, "Fornece coordenadas para o mapa", "JavaScript")

@enduml
```

## Descrição Narrativa

O **Sistema de Rastreamento de Entregas** interage com três tipos de usuários:

- **Entregador** — envia sua posição GPS a cada 3 segundos via `POST /v1/location`
- **Cliente** — recebe atualizações em tempo real via WebSocket e visualiza no mapa (Leaflet + OpenStreetMap, sem custo de API)
- **Administrador** — acessa dashboards de operação e monitora métricas via Grafana

O sistema delega notificações push a FCM e e-mails a SendGrid, mantendo esses canais plugáveis.
