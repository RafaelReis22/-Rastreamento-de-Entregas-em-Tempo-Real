package com.rastreamento.tracking.web.controller;

import com.rastreamento.tracking.infrastructure.kafka.LocationEventProducer;
import com.rastreamento.tracking.infrastructure.redis.GeoSpatialService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/tracking")
public class TrackingController {

    private final LocationEventProducer locationEventProducer;
    private final GeoSpatialService geoSpatialService;
    private final com.rastreamento.tracking.service.GeofenceAlertService geofenceAlertService;

    public TrackingController(LocationEventProducer locationEventProducer,
                              GeoSpatialService geoSpatialService,
                              com.rastreamento.tracking.service.GeofenceAlertService geofenceAlertService) {
        this.locationEventProducer = locationEventProducer;
        this.geoSpatialService = geoSpatialService;
        this.geofenceAlertService = geofenceAlertService;
    }

    @PostMapping("/location")
    public ResponseEntity<Map<String, Object>> receiveLocationPing(@RequestBody Map<String, Object> payload) {
        String deliveryId = (String) payload.getOrDefault("deliveryId", "d100e840-0000-4000-a000-000000000001");
        String delivererId = (String) payload.getOrDefault("delivererId", "u200e840-0000-4000-a000-000000000002");
        double lat = Double.parseDouble(payload.get("latitude").toString());
        double lng = Double.parseDouble(payload.get("longitude").toString());

        geoSpatialService.updateDelivererLocation(delivererId, lat, lng);
        locationEventProducer.publishLocationUpdate(deliveryId, payload);
        geofenceAlertService.checkProximityAndAlert(deliveryId, lat, lng, -23.578500, -46.686000);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("deliveryId", deliveryId);
        response.put("processedAt", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/batch")
    public ResponseEntity<Map<String, Object>> receiveLocationBatch(@RequestBody java.util.List<Map<String, Object>> pings) {
        int count = pings != null ? pings.size() : 0;
        if (pings != null) {
            for (Map<String, Object> ping : pings) {
                String deliveryId = (String) ping.getOrDefault("deliveryId", "d100e840-0000-4000-a000-000000000001");
                locationEventProducer.publishLocationUpdate(deliveryId, ping);
            }
        }
        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("processedBatchSize", count);
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
}
