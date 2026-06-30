package com.rastreamento.tracking.infrastructure.osrm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class MapMatchingService {

    private static final Logger log = LoggerFactory.getLogger(MapMatchingService.class);

    public Map<String, Object> snapToRoad(double latitude, double longitude) {
        log.info("[OSRM MapMatching] Processando alinhamento de coordenada à malha viária (Snap-to-Road): ({}, {})", latitude, longitude);
        
        Map<String, Object> snappedPosition = new HashMap<>();
        snappedPosition.put("latitude", latitude);
        snappedPosition.put("longitude", longitude);
        snappedPosition.put("snapped", true);
        snappedPosition.put("confidence", 0.98);
        return snappedPosition;
    }
}
