package com.rastreamento.tracking.infrastructure.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class GeoSpatialService {

    private static final Logger log = LoggerFactory.getLogger(GeoSpatialService.class);
    private static final String GEO_KEY_DELIVERERS = "deliverers:geo";

    private final StringRedisTemplate redisTemplate;

    public GeoSpatialService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void updateDelivererLocation(String delivererId, double latitude, double longitude) {
        try {
            redisTemplate.opsForGeo().add(GEO_KEY_DELIVERERS, new Point(longitude, latitude), delivererId);
            log.info("[RedisGeo] Posição do entregador '{}' atualizada em Redis Geo: ({}, {})", delivererId, latitude, longitude);
        } catch (Exception e) {
            log.warn("[RedisGeo] Erro ao atualizar posição no Redis Geo (fallback ativo): {}", e.getMessage());
        }
    }
}
