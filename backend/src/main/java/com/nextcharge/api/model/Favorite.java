package com.nextcharge.api.model;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "favorites")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(Favorite.FavoriteId.class)
public class Favorite {
    @Id
    @Column(name = "customer_id")
    private UUID customerId;

    @Id
    @Column(name = "station_id")
    private UUID stationId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt = ZonedDateTime.now();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FavoriteId implements Serializable {
        private UUID customerId;
        private UUID stationId;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = ZonedDateTime.now();
    }
}
