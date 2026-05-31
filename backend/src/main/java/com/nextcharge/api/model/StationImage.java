package com.nextcharge.api.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "station_images")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "station")
public class StationImage {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;

    @Column(name = "image_url", nullable = false, columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt = ZonedDateTime.now();

    @PrePersist
    protected void onCreate() {
        createdAt = ZonedDateTime.now();
    }
}
