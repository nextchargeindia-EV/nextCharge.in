package com.nextcharge.api.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "chargers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "station")
public class Charger {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "connector_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private ConnectorType connectorType;

    @Column(name = "charging_speed_kw", nullable = false)
    private Integer chargingSpeedKw;

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private ChargerStatus status = ChargerStatus.AVAILABLE;

    @Column(name = "price_per_kwh", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerKwh;

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt = ZonedDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt = ZonedDateTime.now();

    public enum ConnectorType {
        CCS2, TYPE2, CHADEMO, GBT
    }

    public enum ChargerStatus {
        AVAILABLE, CHARGING, MAINTENANCE, OFFLINE
    }

    @PrePersist
    protected void onCreate() {
        createdAt = ZonedDateTime.now();
        updatedAt = ZonedDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = ZonedDateTime.now();
    }
}
