package com.rastreamento.delivery.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "order_status_history")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private DeliveryOrder order;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status", columnDefinition = "delivery_status")
    private DeliveryStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, columnDefinition = "delivery_status")
    private DeliveryStatus newStatus;

    @Column(name = "changed_by", nullable = false)
    private UUID changedBy;

    @Column(name = "changed_at", nullable = false, updatable = false)
    private LocalDateTime changedAt;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @PrePersist
    void onCreate() {
        changedAt = LocalDateTime.now();
    }
}
