package com.rastreamento.delivery.infrastructure.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class DeliveryEventProducer {

    private static final Logger log = LoggerFactory.getLogger(DeliveryEventProducer.class);
    private static final String TOPIC_STATUS_CHANGED = "delivery.status.changed";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public DeliveryEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishStatusChanged(UUID deliveryId, UUID delivererId, String oldStatus, String newStatus) {
        Map<String, Object> event = new HashMap<>();
        event.put("deliveryId", deliveryId.toString());
        event.put("delivererId", delivererId != null ? delivererId.toString() : null);
        event.put("oldStatus", oldStatus);
        event.put("newStatus", newStatus);
        event.put("timestamp", System.currentTimeMillis());

        log.info("[DeliveryKafka] Publicando alteração de status da entrega {}: {} -> {}", deliveryId, oldStatus, newStatus);
        kafkaTemplate.send(TOPIC_STATUS_CHANGED, deliveryId.toString(), event);
    }
}
