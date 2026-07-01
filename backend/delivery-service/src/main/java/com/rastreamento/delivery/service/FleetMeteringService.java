package com.rastreamento.delivery.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class FleetMeteringService {

    private static final Logger log = LoggerFactory.getLogger(FleetMeteringService.class);
    private static final double KM_PER_LITER_MOTORCYCLE = 35.0; // 35 km/L em média para motocicletas de entrega
    private static final double FUEL_PRICE_PER_LITER_BRL = 5.89;

    public Map<String, Object> calculateFleetCost(String tenantId, double totalDistanceKm) {
        double estimatedFuelLiters = totalDistanceKm / KM_PER_LITER_MOTORCYCLE;
        double estimatedCostBrl = estimatedFuelLiters * FUEL_PRICE_PER_LITER_BRL;

        log.info("[FleetMetering] Tenant '{}': Distância Total = {} km, Est. Combustível = {} L (R$ {})",
                tenantId, String.format("%.2f", totalDistanceKm), String.format("%.2f", estimatedFuelLiters), String.format("%.2f", estimatedCostBrl));

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("tenantId", tenantId);
        metrics.put("totalDistanceKm", totalDistanceKm);
        metrics.put("estimatedFuelLiters", estimatedFuelLiters);
        metrics.put("estimatedCostBrl", estimatedCostBrl);
        return metrics;
    }
}
