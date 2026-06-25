package com.rastreamento.tracking.domain.entity;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

// PK composta exigida pelo PARTITION BY RANGE do PostgreSQL
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class LocationHistoryId implements Serializable {
    private Long id;
    private LocalDateTime recordedAt;
}
