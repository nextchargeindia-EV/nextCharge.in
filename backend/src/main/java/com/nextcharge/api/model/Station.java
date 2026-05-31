package com.nextcharge.api.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "stations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"owner", "images", "amenities", "chargers"})
public class Station {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String address;

    @Column(nullable = false, precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column(name = "operating_hours", length = 100)
    private String operatingHours = "24/7";

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ApprovalStatus status = ApprovalStatus.PENDING_APPROVAL;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @OneToMany(mappedBy = "station", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StationImage> images;

    @OneToMany(mappedBy = "station", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StationAmenity> amenities;

    @OneToMany(mappedBy = "station", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Charger> chargers;

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt = ZonedDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt = ZonedDateTime.now();

    public enum ApprovalStatus {
        PENDING_APPROVAL, APPROVED, REJECTED
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
