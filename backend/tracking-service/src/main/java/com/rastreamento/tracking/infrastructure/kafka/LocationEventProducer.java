package com.rastreamento.tracking.infrastructure.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class LocationEventProducer {

    private static final Logger log = LoggerFactory.getLogger(LocationEventProducer.class);
    private static final String TOPIC_LOCATION_UPDATED = "location.updated";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public LocationEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishLocationUpdate(String deliveryId, Map<String, Object> locationData) {
        log.info("[TrackingKafka] Publicando ping GPS da entrega '{}' no Kafka: lat={}, lng={}",
                deliveryId, locationData.get("latitude"), locationData.get("longitude"));
        kafkaTemplate.send(TOPIC_LOCATION_UPDATED, deliveryId, locationData);
    }
}
