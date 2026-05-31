package com.nextcharge.api.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "fleet_vehicles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "fleetManager")
public class FleetVehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fleet_manager_id", nullable = false)
    private User fleetManager;

    @Column(name = "plate_number", nullable = false, unique = true, length = 20)
    private String plateNumber;

    @Column(nullable = false, length = 100)
    private String model;

    @Column(name = "driver_name", nullable = false, length = 100)
    private String driverName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt = ZonedDateTime.now();

    @PrePersist
    protected void onCreate() {
        createdAt = ZonedDateTime.now();
    }
}
