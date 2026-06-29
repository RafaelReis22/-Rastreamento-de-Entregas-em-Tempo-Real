package com.rastreamento.websocket.infrastructure.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class KafkaLocationConsumer {

    private static final Logger log = LoggerFactory.getLogger(KafkaLocationConsumer.class);
    private final SimpMessagingTemplate messagingTemplate;

    public KafkaLocationConsumer(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @KafkaListener(topics = "location.updated", groupId = "websocket-group")
    public void consumeLocationUpdate(Map<String, Object> locationData) {
        String deliveryId = (String) locationData.getOrDefault("deliveryId", "d100e840-0000-4000-a000-000000000001");
        String destination = "/topic/tracking/" + deliveryId;

        log.info("[WebSocketKafka] Recebido ping GPS do Kafka para entrega '{}', retransmitindo via STOMP para '{}'", deliveryId, destination);
        messagingTemplate.convertAndSend(destination, locationData);
    }
}
