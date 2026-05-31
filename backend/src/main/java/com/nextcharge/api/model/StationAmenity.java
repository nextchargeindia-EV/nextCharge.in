package com.nextcharge.api.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "station_amenities")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "station")
public class StationAmenity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;

    @Column(name = "amenity_name", nullable = false, length = 100)
    @Enumerated(EnumType.STRING)
    private AmenityType amenityName;

    public enum AmenityType {
        RESTROOM, CAFE, WIFI, PARKING, WAITING_AREA
    }
}
