package com.rastreamento.tracking.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "location_history")
@IdClass(LocationHistoryId.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationHistory {

    // BIGSERIAL — gerado pela sequence location_history_id_seq do PostgreSQL
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "location_history_seq")
    @SequenceGenerator(
        name = "location_history_seq",
        sequenceName = "location_history_id_seq",
        allocationSize = 50
    )
    private Long id;

    // Parte da PK composta — obrigatória para tabelas particionadas por range
    @Id
    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "deliverer_id", nullable = false)
    private UUID delivererId;

    @Column(nullable = false, precision = 10, scale = 8)
    private BigDecimal lat;

    @Column(nullable = false, precision = 11, scale = 8)
    private BigDecimal lng;

    @Column(name = "accuracy_m", precision = 6, scale = 2)
    private BigDecimal accuracyM;

    @Column(name = "is_suspicious", nullable = false)
    @Builder.Default
    private boolean suspicious = false;

    @PrePersist
    void onCreate() {
        if (recordedAt == null) {
            recordedAt = LocalDateTime.now();
        }
    }
}
