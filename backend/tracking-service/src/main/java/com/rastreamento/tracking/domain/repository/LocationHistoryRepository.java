package com.rastreamento.tracking.domain.repository;

import com.rastreamento.tracking.domain.entity.LocationHistory;
import com.rastreamento.tracking.domain.entity.LocationHistoryId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface LocationHistoryRepository extends JpaRepository<LocationHistory, LocationHistoryId> {
    Page<LocationHistory> findByOrderIdOrderByRecordedAtDesc(UUID orderId, Pageable pageable);

    // LGPD: expurga registros com mais de 90 dias
    @Modifying
    @Query("DELETE FROM LocationHistory l WHERE l.recordedAt < :threshold")
    void deleteOlderThan(LocalDateTime threshold);
}
