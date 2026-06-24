# ADR-004: WebSocket com STOMP sobre SockJS

**Data:** 2026-06-24  
**Status:** Aceito  

---

## Contexto

O cliente precisa receber atualizações de localização do entregador em tempo real (< 1s de latência). A solução deve funcionar em ambientes com proxies, firewalls corporativos e navegadores antigos que bloqueiam WebSocket puro.

## Decisão

**Spring WebSocket + protocolo STOMP + SockJS como fallback.**

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins("*")
                .withSockJS();  // fallback automático
    }
}
```

**Tópico por pedido:** `/topic/order/{orderID}`

**Heartbeat:** 25s/25s para detecção de conexão morta.

**Reconexão no cliente:** backoff exponencial (1s → 2s → 4s → 8s → máx 30s).

**Autenticação:** token JWT validado no handshake WebSocket antes de aceitar a conexão.

## Consequências

**Positivas:**
- STOMP fornece tópicos nomeados, subscriptions padronizadas e ACK
- SockJS garante compatibilidade universal (HTTP long-polling como fallback)
- Sticky session no Load Balancer permite múltiplas réplicas do `websocket-service`
- Reconexão automática transparente para o usuário

**Negativas:**
- Sticky session limita distribuição perfeita de carga (aceitável para MVP)
- Overhead do protocolo STOMP vs WebSocket puro (~200 bytes/frame)

## Alternativas Consideradas

- **Server-Sent Events (SSE):** descartado — unidirecional, não suporta ACK do cliente
- **WebSocket puro sem STOMP:** descartado — sem gerenciamento de tópicos e subscriptions padronizadas
- **Long Polling:** descartado — latência muito alta para atualização de mapa em tempo real

## Referências

- [ADR-003](ADR-003-kafka-eventos.md) — Kafka alimenta o WebSocket Service
- [ADR-005](ADR-005-jwt-refresh-token.md) — JWT autentica o handshake
