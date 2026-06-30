package com.rastreamento.delivery.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class EtaCalculatorService {

    private static final Logger log = LoggerFactory.getLogger(EtaCalculatorService.class);

    public static class EtaResult {
        private final int estimatedMinutes;
        private final String trafficCondition;
        private final LocalDateTime estimatedArrivalTime;

        public EtaResult(int estimatedMinutes, String trafficCondition, LocalDateTime estimatedArrivalTime) {
            this.estimatedMinutes = estimatedMinutes;
            this.trafficCondition = trafficCondition;
            this.estimatedArrivalTime = estimatedArrivalTime;
        }

        public int getEstimatedMinutes() { return estimatedMinutes; }
        public String getTrafficCondition() { return trafficCondition; }
        public LocalDateTime getEstimatedArrivalTime() { return estimatedArrivalTime; }
    }

    public EtaResult calculateDynamicEta(double currentLat, double currentLng, double destLat, double destLng) {
        // Distância Haversine simplificada
        double latDiff = Math.toRadians(destLat - currentLat);
        double lngDiff = Math.toRadians(destLng - currentLng);
        double a = Math.sin(latDiff / 2) * Math.sin(latDiff / 2) +
                Math.cos(Math.toRadians(currentLat)) * Math.cos(Math.toRadians(destLat)) *
                        Math.sin(lngDiff / 2) * Math.sin(lngDiff / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distanceKm = 6371 * c;

        // Fator de velocidade média urbana (30 km/h)
        int estimatedMinutes = Math.max(2, (int) Math.ceil((distanceKm / 30.0) * 60));
        LocalDateTime etaTime = LocalDateTime.now().plusMinutes(estimatedMinutes);

        log.info("[ETA ML] Distância: {} km -> ETA Estimado: {} minutos (Chegada prevista: {})",
                String.format("%.2f", distanceKm), estimatedMinutes, etaTime);

        return new EtaResult(estimatedMinutes, "NORMAL", etaTime);
    }
}
