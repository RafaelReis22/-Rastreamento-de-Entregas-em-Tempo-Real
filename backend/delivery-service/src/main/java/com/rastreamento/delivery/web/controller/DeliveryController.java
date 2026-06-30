package com.rastreamento.delivery.web.controller;

import com.rastreamento.delivery.domain.entity.DeliveryOrder;
import com.rastreamento.delivery.domain.entity.DeliveryStatus;
import com.rastreamento.delivery.infrastructure.kafka.DeliveryEventProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/v1/orders")
public class DeliveryController {

    private static final Logger log = LoggerFactory.getLogger(DeliveryController.class);
    private final DeliveryEventProducer deliveryEventProducer;
    private final Map<UUID, DeliveryOrder> mockDatabase = new HashMap<>();

    public DeliveryController(DeliveryEventProducer deliveryEventProducer) {
        this.deliveryEventProducer = deliveryEventProducer;
        initMockData();
    }

    private void initMockData() {
        UUID id1 = UUID.fromString("d100e840-0000-4000-a000-000000000001");
        DeliveryOrder order1 = DeliveryOrder.builder()
                .id(id1)
                .customerId(UUID.fromString("c100e840-0000-4000-a000-000000000001"))
                .delivererId(UUID.fromString("u200e840-0000-4000-a000-000000000002"))
                .status(DeliveryStatus.IN_TRANSIT)
                .originAddress("Av. Paulista, 1578 - São Paulo, SP")
                .destAddress("R. Oscar Freire, 950 - São Paulo, SP")
                .originLat(-23.561684)
                .originLng(-46.655981)
                .destLat(-23.568200)
                .destLng(-46.667500)
                .build();
        mockDatabase.put(id1, order1);
    }

    @GetMapping
    public ResponseEntity<List<DeliveryOrder>> listOrders() {
        return ResponseEntity.ok(new ArrayList<>(mockDatabase.values()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeliveryOrder> getOrder(@PathVariable UUID id) {
        DeliveryOrder order = mockDatabase.get(id);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(order);
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<DeliveryOrder> updateStatus(@PathVariable UUID id, @RequestParam String status) {
        DeliveryOrder order = mockDatabase.get(id);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        String oldStatus = order.getStatus().name();
        order.setStatus(DeliveryStatus.valueOf(status.toUpperCase()));
        
        deliveryEventProducer.publishStatusChanged(id, order.getDelivererId(), oldStatus, status);
        return ResponseEntity.ok(order);
    }
}
