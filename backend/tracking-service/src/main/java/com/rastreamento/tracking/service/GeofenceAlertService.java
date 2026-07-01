package com.rastreamento.tracking.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class GeofenceAlertService {

    private static final Logger log = LoggerFactory.getLogger(GeofenceAlertService.class);
    private static final String TOPIC_NOTIFICATION = "notification.send";
    private static final double GEOFENCE_RADIUS_METERS = 500.0;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public GeofenceAlertService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void checkProximityAndAlert(String deliveryId, double currentLat, double currentLng, double destLat, double destLng) {
        double distanceMeters = calculateDistanceMeters(currentLat, currentLng, destLat, destLng);

        if (distanceMeters <= GEOFENCE_RADIUS_METERS) {
            log.info("[Geofencing] Entregador dentro do raio de {}m ({:.1f}m do destino) para a entrega '{}'. Disparando notificação!",
                    GEOFENCE_RADIUS_METERS, distanceMeters, deliveryId);

            Map<String, Object> notifPayload = new HashMap<>();
            notifPayload.put("deliveryId", deliveryId);
            notifPayload.put("recipient", "cliente@exemplo.com");
            notifPayload.put("message", "Seu entregador está super próximo ao seu endereço! Fique atento(a).");
            notifPayload.put("channel", "SMS_AND_PUSH");

            kafkaTemplate.send(TOPIC_NOTIFICATION, deliveryId, notifPayload);
        }
    }

    private double calculateDistanceMeters(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371000; // Raio da Terra em metros
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
