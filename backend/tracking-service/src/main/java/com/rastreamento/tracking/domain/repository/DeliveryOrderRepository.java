package com.rastreamento.tracking.domain.repository;

import com.rastreamento.tracking.domain.entity.DeliveryOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DeliveryOrderRepository extends JpaRepository<DeliveryOrder, UUID> {
}
