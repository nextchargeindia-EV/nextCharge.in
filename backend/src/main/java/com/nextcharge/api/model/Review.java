package com.nextcharge.api.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "reviews", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"customer_id", "station_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"customer", "station"})
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;

    @Column(nullable = false)
    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt = ZonedDateTime.now();

    @PrePersist
    protected void onCreate() {
        createdAt = ZonedDateTime.now();
    }
}
