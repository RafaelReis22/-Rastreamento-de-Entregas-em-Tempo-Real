package com.rastreamento.delivery.domain.repository;

import com.rastreamento.delivery.domain.entity.DeliveryOrder;
import com.rastreamento.delivery.domain.entity.DeliveryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DeliveryOrderRepository extends JpaRepository<DeliveryOrder, UUID> {
    Page<DeliveryOrder> findByCustomerIdOrderByCreatedAtDesc(UUID customerId, Pageable pageable);
    Page<DeliveryOrder> findByDelivererIdOrderByCreatedAtDesc(UUID delivererId, Pageable pageable);
    List<DeliveryOrder> findByDelivererIdAndStatus(UUID delivererId, DeliveryStatus status);
}
