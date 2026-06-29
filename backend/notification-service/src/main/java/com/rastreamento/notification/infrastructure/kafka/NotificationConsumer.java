package com.rastreamento.notification.infrastructure.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class NotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);

    @KafkaListener(topics = "notification.send", groupId = "notification-group")
    public void consumeNotificationEvent(Map<String, Object> payload) {
        String recipient = (String) payload.getOrDefault("recipient", "cliente@exemplo.com");
        String message = (String) payload.getOrDefault("message", "Seu pedido está a caminho!");
        String channel = (String) payload.getOrDefault("channel", "SMS");

        log.info("[NotificationService] Enviando notificação [{}] para '{}': {}", channel, recipient, message);
    }
}
