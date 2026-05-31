package com.nextcharge.api.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"customer", "charger"})
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "charger_id", nullable = false)
    private Charger charger;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private BookingStatus status = BookingStatus.PENDING;

    @Column(name = "total_energy_kwh", precision = 10, scale = 2)
    private BigDecimal totalEnergyKwh = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt = ZonedDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt = ZonedDateTime.now();

    public enum BookingStatus {
        PENDING, CONFIRMED, ACTIVE, COMPLETED, CANCELLED
    }

    @PrePersist
    protected void onCreate() {
        createdAt = ZonedDateTime.now();
        updatedAt = ZonedDateTime.now();
        if (totalEnergyKwh == null) {
            totalEnergyKwh = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = ZonedDateTime.now();
    }
}
